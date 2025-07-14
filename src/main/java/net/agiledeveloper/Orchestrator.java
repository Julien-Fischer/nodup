package net.agiledeveloper;

import net.agiledeveloper.image.ImageDeduplicator;

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
        if (isHelpMessage(args)) {
            printHelp();
        } else if(isOpenBin(args)) {
            openBin();
        } else {
            parseArguments(args);
            processCommand(args);
        }
    }

    private void processCommand(String[] args) {
        setLogLevel(LOG_LEVEL);

        var directory = readDirectory(args);
        logConfig(directory);

        imageDeduplicator.execute(action, directory);
    }

    private static boolean isOpenBin(String[] arguments) {
        return asList(arguments).contains("--bin");
    }

    private void openBin() {
        directoryOpener.open(imageDeduplicator.binRoot());
    }

    private static boolean isHelpMessage(String[] args) {
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

    private static String readDirectory(String[] args) {
        boolean isDefined = args.length > 0 && !args[0].startsWith("-");
        return isDefined ? args[0] : System.getProperty("user.dir");
    }

    private void logConfig(String directory) {
        logger.info(() -> "%s duplicates in %s".formatted(action, directory));
        logger.info(() -> "Image processor: %s".formatted(PROCESSOR));
        logger.info(() -> "Collision algorithm: %s".formatted(COLLIDER));
        logger.info(() -> "Log level: %s".formatted(LOG_LEVEL));
        logger.info(() -> "Bin: %s".formatted(imageDeduplicator.binRoot()));
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
        String[] supported = {"--help", "-h", "--scan", "-s", "--copy", "-c", "--move", "-m", "--log", "--bin"};
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
