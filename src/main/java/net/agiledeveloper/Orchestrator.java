package net.agiledeveloper;

import net.agiledeveloper.image.ImageDeduplicator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static net.agiledeveloper.App.*;

public class Orchestrator {

    private final ImageDeduplicator imageDeduplicator;
    private final DirectoryOpener directoryOpener;
    private Action action = App.DEFAULT_ACTION;


    public Orchestrator(ImageDeduplicator imageDeduplicator, DirectoryOpener directoryOpener) {
        this.imageDeduplicator = imageDeduplicator;
        this.directoryOpener = directoryOpener;
    }


    public void execute(String[] args) {
        if (isHelpRequest(args)) {
            printHelp();
        } else if (isBinCommand(args)) {
            processBin(args);
        } else {
            parseArguments(args);
            processCommand(args);
        }
    }

    private void processBin(String[] arguments) {
        if (isOpenBinRequest(arguments)) {
            openBin();
        } else if (isListBinDirectories(arguments)) {
            listBinDirectories();
        } else if (isClearBin(arguments)) {
            imageDeduplicator.bin().clear();
        } else if (isPrintBinPathRequest(arguments)) {
            printBinPath();
        } else {
            printHelp();
        }
    }

    @SuppressWarnings("java:S106")
    private void printBinPath() {
        System.out.println(imageDeduplicator.bin().root());
    }

    private boolean isPrintBinPathRequest(String[] arguments) {
        return asList(arguments).contains("--path");
    }

    private boolean isBinCommand(String[] arguments) {
        return asList(arguments).contains("bin");
    }

    private boolean isClearBin(String[] arguments) {
        return asList(arguments).contains("--clear");
    }

    @SuppressWarnings("java:S106")
    private void listBinDirectories() {
        List<Path> directories = imageDeduplicator.bin().directories();
        System.out.println("Bins: " + directories.size());
        directories.forEach(directory -> System.out.println("- " + directory.toAbsolutePath()));
    }

    private boolean isListBinDirectories(String[] arguments) {
        return asList(arguments).contains("--list");
    }

    private void processCommand(String[] args) {
        setLogLevel(LOG_LEVEL);

        Path directory = requireValid(readDirectory(args));
        logConfig(directory);

        imageDeduplicator.execute(action, directory);
    }

    private static Path requireValid(Path directory) {
        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("Could not find specified directory: " + directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Specified path is not a directory: " + directory);
        }
        return directory;
    }

    private static boolean isOpenBinRequest(String[] arguments) {
        return asList(arguments).contains("--open");
    }

    private void openBin() {
        directoryOpener.open(imageDeduplicator.bin().root());
    }

    private static boolean isHelpRequest(String[] args) {
        return stream(args)
                .anyMatch(argument -> argument.equals("-h") || argument.equals("--help"));
    }

    @SuppressWarnings("java:S106")
    public static void printHelp() {
        System.out.println("""
Usage:
  nodup [/path/to/dir] [OPTIONS]

Positional parameters:
  $1               (Optional) The path to the directory to process

Options:
  --log            Set the logging level (e.g., severe, warning, info, fine, finer, finest).

Flags:
  -c, --copy       Copy files in the directory.
  -m, --move       Move files in the directory.
  -s, --scan       Scan the directory and display file information.
  -h, --help       Print this help message and exit
  --bin            Open the bin directory (requires a GUI environment)
""");
    }

    private static Path readDirectory(String[] args) {
        boolean isDefined = args.length > 0 && !args[0].startsWith("-");
        String directory = isDefined ? args[0] : System.getProperty("user.dir");
        return Paths.get(directory);
    }

    private void logConfig(Path directory) {
        logger.info(() -> "%s duplicates in %s".formatted(action, directory));
        logger.info(() -> "Image processor: %s".formatted(PROCESSOR));
        logger.info(() -> "Collision algorithm: %s".formatted(COLLIDER));
        logger.info(() -> "Log level: %s".formatted(LOG_LEVEL));
    }

    private void parseArguments(String[] arguments) {
        Optional<IllegalArgumentException> unknownArgument = findUnknownArguments(arguments);
        if (unknownArgument.isPresent()) {
            throw unknownArgument.get();
        }

        processLogLevel(arguments);

        for (String argument : arguments) {
            action = readAction(argument);
        }
    }

    private static Optional<IllegalArgumentException> findUnknownArguments(String[] arguments) {
        String[] supported = {
                "--help", "-h",
                "--scan", "-s",
                "--copy", "-c",
                "--move", "-m",
                "--log",
                "bin", "--open", "--list", "--clear", "--path"
        };
        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            if (i == 0 && !argument.startsWith("-")) {
                continue;
            }
            var isSupported = asList(supported).contains(argument) || argument.startsWith("--log=");
            if (!isSupported) {
                return Optional.of(new IllegalArgumentException("Unknown argument: " + argument));
            }
        }
        return Optional.empty();
    }


    private static void processLogLevel(String[] arguments) {
        stream(arguments)
                .filter(argument -> argument.startsWith("--log="))
                .findFirst()
                .map(Orchestrator::parseLogLevel)
                .ifPresent(Orchestrator::setLogLevel);
    }

    public static void setLogLevel(Level level) {
        LOG_LEVEL = level;
        logger.setLevel(level);
        for (var handler : Logger.getLogger("").getHandlers()) {
            handler.setFormatter(new MessageFormatter());
            handler.setLevel(level);
        }
    }

    private static Level parseLogLevel(String argument) {
        String levelString = argument.substring("--log=".length());
        return readLogLevel(levelString);
    }

    public static Level readLogLevel(String levelName) {
        if (levelName == null) {
            throw new IllegalArgumentException("Level name cannot be null");
        }
        try {
            return Level.parse(levelName.trim().toUpperCase());
        } catch (IllegalArgumentException cause) {
            throw new IllegalArgumentException("Unknown logging level: " + levelName, cause);
        }
    }

    private static App.Action readAction(String argument) {
        return switch (argument) {
            case "-c", "--copy" -> App.Action.COPY;
            case "-m", "--move" -> App.Action.MOVE;
            case "-s", "--scan" -> App.Action.SCAN;
            default -> DEFAULT_ACTION;
        };
    }

}
