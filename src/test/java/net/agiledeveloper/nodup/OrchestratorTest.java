package net.agiledeveloper.nodup;

import net.agiledeveloper.nodup.bin.Bin;
import net.agiledeveloper.nodup.bin.Bin.PathProvider;
import net.agiledeveloper.nodup.image.Image;
import net.agiledeveloper.nodup.image.ImageDeduplicator;
import net.agiledeveloper.nodup.image.ImageProvider;
import net.agiledeveloper.nodup.image.processors.BucketProcessor;
import net.agiledeveloper.nodup.image.processors.ExifProcessor;
import net.agiledeveloper.nodup.ui.DirectoryOpener;
import net.agiledeveloper.stubs.StubImage.ImageBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.String.join;
import static net.agiledeveloper.stubs.StubImage.ImageBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class OrchestratorTest {

    @TempDir
    private Path tempDir;
    private Path directoryToScan;

    private TestHandler handler;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    private final StubImageProvider imageProvider = new StubImageProvider();
    private final StubDirectoryOpener directoryOpener = new StubDirectoryOpener();

    private PathProvider pathProvider;
    private Bin bin;
    private Orchestrator orchestrator;

    private final List<Class<?>> loggersToMock = List.of(
            App.class,
            Bin.class,
            Orchestrator.class,
            ImageDeduplicator.class,
            BucketProcessor.class
    );


    @BeforeEach
    void setUp() {
        mockStdout();
        mockLogger();
        clearStubs();
        buildOrchestrator();
    }


    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }


    @Nested
    class ParametersTest {

        @Test
        void print_an_help_message() {
            whenStartingApp()
                    .withParameters("--help");

            expectStdout()
                    .toContainHelpMessage();
        }

        @Test
        void use_current_directory_by_default() throws IOException {
            havingDirectoryNamed("directory");

            whenStartingApp()
                    .withoutAnyParameter();

            expectLog()
                    .toContain(System.getProperty("user.dir"));
        }

        @Test
        void use_default_log_level() throws IOException {
            havingDirectoryNamed("directory");

            whenStartingApp()
                    .withParameters(directoryToScan.toString());

            expectLog()
                    .toContain("Log level: INFO");
        }

        @Test
        void use_specified_log_level() throws IOException {
            havingDirectoryNamed("directory");

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--log=fine");

            expectLog()
                    .toContain("Log level: FINE");
        }

        @Test
        void use_specified_log_level_when_no_positional_parameters() throws IOException {
            havingDirectoryNamed("directory");

            whenStartingApp()
                    .withParameters("--log=fine");

            expectLog()
                    .toContain(System.getProperty("user.dir"))
                    .toContain("Log level: FINE");
        }

        @Test
        void unknown_arguments_throw() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> whenStartingApp().withParameters("--unknown-argument"))
                    .withMessageContaining("Unknown argument: --unknown-argument");
        }

        @Test
        void unknown_directories_can_not_be_processed() throws IOException {
            havingDirectoryNamed("directory")
                    .empty();

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> whenStartingApp().withParameters("/invalid/directory"))
                    .withMessageContaining("Could not find specified directory: /invalid/directory");
        }

        @Test
        void invalid_directory_paths_can_not_be_processed() throws IOException {
            var textFile = aTextFile("a/text/file.txt");
            havingDirectoryNamed("directory")
                    .containing(textFile);
            String filePath = textFile.fullyQualifiedPath().toString();

            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> whenStartingApp().withParameters(filePath))
                    .withMessageContaining("Specified path is not a directory: " + filePath);
        }

    }

    @Nested
    class CollisionDetectionTest {

        @Test
        void with_no_images() throws IOException {
            havingDirectoryNamed("directory")
                    .empty();

            whenStartingApp()
                    .withParameters(directoryToScan.toString());

            assertThatNoFilesWereFound();
        }

        @Test
        void with_images_but_no_collision() throws IOException {
            havingDirectoryNamed("directory")
                    .containing(aBigDog(), aDog(), aCat());

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--log=fine");

            assertThatFilesWereFound(3);
            assertThatNoDuplicatesWereFound();
        }

        @Test
        void with_collisions() throws IOException {
            var a = aDogImage().named("dog-a").build();
            var b = aDogImage().named("dog-b").build();
            havingDirectoryNamed("directory")
                    .containing(aBigDog(), a, b, aCat());

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--log=finer");

            assertThatDuplicatesWereFound(1)
                    .forImages(a, b);
        }

        @Test
        void duplicates_are_moved_to_bin() throws IOException {
            havingDirectoryToScan("directory");
            var dogA = aDogImage().located(directoryToScan).named("dog-a").build();
            var dogB = aDogImage().located(directoryToScan).named("dog-b").build();
            var catA = aCatImage().located(directoryToScan).named("cat-a").build();
            var catB = aCatImage().located(directoryToScan).named("cat-b").build();
            givenThat(directoryToScan)
                    .contains(aBigDog(), dogA, catA, catB, dogB);

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--move");

            assertThatDuplicatesWereMoved(2)
                    .forImages(catA, dogA);
        }

        @Test
        void duplicates_are_copied_to_bin() throws IOException {
            havingDirectoryToScan("directory");
            var a = aDogImage().located(directoryToScan).named("dog-a").build();
            var b = aDogImage().located(directoryToScan).named("dog-b").build();
            givenThat(directoryToScan)
                    .contains(aBigDog(), a, b, aCat());

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--copy");

            assertThatDuplicatesWereCopied(1)
                    .forImages(a);
        }

        @Test
        void multiple_collisions_are_supported() throws IOException {
            havingDirectoryToScan("directory");
            var a = aDogImage().located(directoryToScan).named("dog-a").build();
            var b = aDogImage().located(directoryToScan).named("dog-b").build();
            var c = aDogImage().located(directoryToScan).named("dog-c").build();
            givenThat(directoryToScan)
                    .contains(aBigDog(), a, b, c, aCat());

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--copy");

            assertThatDuplicatesWereCopied(2)
                    .forImages(a, b);
        }

    }

    @Nested
    class BinTest {

        @Test
        void open_bin_directory() {
            whenStartingApp()
                    .withParameters("bin", "--open");

            expect(directoryOpener)
                    .toHaveOpened(bin.root());
        }

        @Test
        void only_create_bin_directory_on_duplicate_copy() throws IOException {
            havingDirectoryToScan("directory");
            var a = aDogImage().located(directoryToScan).named("dog-a").build();
            var b = aDogImage().located(directoryToScan).named("dog-b").build();
            givenThat(directoryToScan)
                    .contains(a, b);

            expect(bin).toBeEmpty();

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--copy");

            expect(bin).toContain(b);
        }

        @Test
        void only_create_bin_directory_on_duplicate_move() throws IOException {
            havingDirectoryToScan("directory");
            var a = aDogImage().located(directoryToScan).named("dog-a").build();
            var b = aDogImage().located(directoryToScan).named("dog-b").build();
            givenThat(directoryToScan)
                    .contains(a, b);

            expect(bin).toBeEmpty();

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--move");

            expect(bin).toContain(b);
        }

        @Test
        void create_bin_directory_on_duplicate_scan() throws IOException {
            givenThat(aDogImage()).hasDuplicates(1);

            expect(bin).toBeEmpty();

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "--scan");

            expect(bin).toBeEmpty();
        }

        @Test
        void list_bin_directories() throws IOException {
            havingBinDirectories(1);

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "bin", "--list");

            expectStdout()
                    .toPartiallyMatch("/bin/current$");
        }

        @Test
        void clear_deletes_all_bin_directories() throws IOException {
            havingBinDirectories(2);

            whenStartingApp()
                    .withParameters(directoryToScan.toString(), "bin", "--clear");

            expect(bin).toBeEmpty();
        }

        @Test
        void bin_path_prints_bin_path() {
            whenStartingApp()
                    .withParameters("bin", "--path");

            expectStdout()
                    .toContain("/bin");
        }

        @Test
        void bin_prints_bin_path() {
            whenStartingApp()
                    .withParameters("bin");

            expectStdout()
                    .toContainHelpMessage();
        }

    }


    private void havingBinDirectories(int count) throws IOException {
        givenThat(aDogImage()).hasDuplicates(count);
        whenStartingApp()
                .withParameters(directoryToScan.toString(), "--copy");
    }

    private ImageDuplication givenThat(ImageBuilder image) {
        return new ImageDuplication(image);
    }

    private class ImageDuplication {

        private final ImageBuilder original;

        public ImageDuplication(ImageBuilder original) {
            this.original = original;
        }

        void hasDuplicates(int times) throws IOException {
            havingDirectoryToScan("directory");
            ImageBuilder prototype = original.located(directoryToScan);
            givenThat(directoryToScan)
                    .contains(prototype.build());
            for (int i = 1; i <= times; i++) {
                Image duplicate = prototype
                        .named(original.name() + "-" + i)
                        .build();
                givenThat(directoryToScan)
                        .contains(duplicate);
            }
        }

    }

    private BinAssertion expect(Bin bin) {
        return new BinAssertion(pathProvider, bin);
    }

    private record BinAssertion(PathProvider pathProvider, Bin bin) {

        public void toBeEmpty() {
            assertThat(bin.isEmpty()).isTrue();
            assertThat(Files.exists(currentBin())).isFalse();
        }

        public void toNotBeEmpty() {
            assertThat(bin.isEmpty()).isFalse();
            assertThat(Files.exists(currentBin())).isTrue();
        }

        public void toContain(Image image) {
            try (var stream = Files.list(currentBin())) {
                var list = stream.filter(Files::isRegularFile).toList();
                boolean contains = list.stream()
                        .anyMatch(p -> p.getFileName().equals(image.path().getFileName()));
                assertThat(contains)
                        .withFailMessage(format(
                                "Current bin does not contain %s. Content: %s elements: %s",
                                image.path(), list.size(), list
                        ))
                        .isTrue();
            } catch (IOException cause) {
                throw new RuntimeException("listFiles failed: " + cause, cause);
            }
        }

        private Path currentBin() {
            return pathProvider.currentBin();
        }

        public void toHaveDirectories(int expected) {
            File[] directories = pathProvider.root().toFile().listFiles(File::isDirectory);
            int actual = directories == null ? 0 : directories.length;
            if (actual != expected) {
                throw new AssertionError(format(
                        "Expected %s directories but got %s %s",
                        expected, actual, Arrays.toString(directories)
                ));
            }
            assertThat(directories).hasSize(expected);
        }

    }

    private record TextFile(Path parentDirectory, Path path) {

        public Path fullyQualifiedPath() {
            return parentDirectory.resolve(path);
        }

    }

    private TextFile aTextFile(String path) {
        return new TextFile(tempDir, Paths.get(path));
    }

    private DirectoryOpenerAssertion expect(StubDirectoryOpener directoryOpener) {
        return new DirectoryOpenerAssertion(directoryOpener);
    }

    private record DirectoryOpenerAssertion(StubDirectoryOpener directoryOpener) {

        public void toHaveOpened(Path expected) {
            assertThat(directoryOpener.openedDirectories).contains(expected);
        }

    }

    private AppAction whenStartingApp() {
        return new AppAction();
    }

    private class AppAction {

        void withoutAnyParameter() {
            orchestrator.execute(new String[0]);
        }

        void withParameters(String... parameters) {
            orchestrator.execute(parameters);
        }

    }

    private FilePrecondition givenThat(Path directory) {
        return new FilePrecondition(imageProvider, directory);
    }

    private FilePrecondition havingDirectoryNamed(String directory) throws IOException {
        havingDirectoryToScan(directory);
        return new FilePrecondition(imageProvider, directoryToScan);
    }

    private void havingDirectoryToScan(String directory) throws IOException {
        directoryToScan = tempDir.resolve(directory);
        Files.createDirectory(directoryToScan);
    }

    private void assertThatFilesWereFound(int count) {
        expectLog()
                .toContain("Found %s (potential) images in %s".formatted(count, directoryToScan));
    }

    private void assertThatNoFilesWereFound() {
        assertThatFilesWereFound(0);
    }

    private void assertThatNoDuplicatesWereFound() {
        expectLog().toContain("Found 0 collisions");
    }

    private StdoutAssertion expectStdout() {
        return new StdoutAssertion(outputStream, originalOut);
    }

    private record StdoutAssertion(OutputStream outputStream, PrintStream originalOut) {

        public StdoutAssertion toContain(String expected) {
            try {
                assertThat(outputStream.toString()).contains(expected);
            } finally {
                System.setOut(originalOut);
            }
            return this;
        }

        public StdoutAssertion toPartiallyMatch(String patternString) {
            var subject = outputStream.toString();
            var pattern = Pattern.compile(patternString, Pattern.DOTALL);
            if (!pattern.matcher(subject).find()) {
                try {
                    throw new AssertionError(format(
                            "Subject %n%s does not match pattern %s",
                            subject, pattern
                    ));
                } finally {
                    System.setOut(originalOut);
                }
            }
            return this;
        }

        public void toContainHelpMessage() {
            new StdoutAssertion(outputStream, originalOut)
                    .toContain("nodup [/path/to/dir] [OPTIONS]")
                    .toContain("Positional parameters:")
                    .toContain("$1               (Optional) The path to the directory to process")
                    .toContain("Options:")
                    .toContain("--log            Set the logging level (e.g., severe, warning, info, fine, finer, finest).")
                    .toContain("Flags:");
        }
    }

    private LogAssertion expectLog() {
        return new LogAssertion(handler, originalOut);
    }

    private record LogAssertion(TestHandler handler, PrintStream originalOut) {

        public LogAssertion toContain(String expected) {
            try {
                assertThat(handler.getMessages()).contains(expected);
            } finally {
                System.setOut(originalOut);
            }
            return this;
        }

    }

    private void clearStubs() {
        imageProvider.clear();
        directoryOpener.clear();
    }

    private void mockLogger() {
        handler = new TestHandler();
        loggersToMock.forEach(type -> {
            var logger = Logger.getLogger(type.getSimpleName());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
        });
    }

    private void mockStdout() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private void buildOrchestrator() {
        pathProvider = new StubPathProvider(tempDir);
        bin = new Bin(pathProvider);
        var imageProcessor = new ExifProcessor();
        var imageDeduplicator = new ImageDeduplicator(imageProcessor, imageProvider, bin);
        orchestrator = new Orchestrator(imageDeduplicator, directoryOpener);
    }


    static class TestHandler extends Handler {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void publish(LogRecord logRecord) {
            messages.add(logRecord.getMessage());
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

        public String getMessages() {
            return join(" | ", messages);
        }
    }

    private record FilePrecondition(StubImageProvider imageProvider, Path directory) {

        public void empty() {}

        public void contains(Image... images) throws IOException {
            containing(images);
        }

        public void containing(Image... images) throws IOException {
            for (var image : images) {
                imageProvider.addImage(image);
                Path fileToCreate = directory.resolve(image.path());
                Files.createDirectories(fileToCreate.getParent());
                Files.createFile(fileToCreate);
            }
        }

        public void containing(TextFile... files) throws IOException {
            for (var file : files) {
                Path fileToCreate = file.fullyQualifiedPath();
                Path parent = fileToCreate.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.createFile(fileToCreate);
            }
        }

    }

    private CopyAssertion assertThatDuplicatesWereCopied(int count) {
        expectLog()
                .toContain("About to [COPY] %d duplicates to %s".formatted(count, pathProvider.currentBin()));
        return new CopyAssertion();
    }

    private class CopyAssertion {

        public void forImages(Image... images) {
            expectLog()
                    .toContain("Done [COPY] %s duplicates to %s".formatted(images.length, pathProvider.currentBin()));
        }

    }

    private MoveAssertion assertThatDuplicatesWereMoved(int count) {
        expectLog()
                .toContain("About to [MOVE] %d duplicates to %s".formatted(count, pathProvider.currentBin()));
        return new MoveAssertion();
    }

    private class MoveAssertion {

        public void forImages(Image... images) {
            expectLog()
                    .toContain("Done [MOVE] %s duplicates to %s".formatted(images.length, pathProvider.currentBin()));
        }

    }


    private DuplicateAssertion assertThatDuplicatesWereFound(int count) {
        expectLog()
                .toContain("Found %d collisions".formatted(count));
        return new DuplicateAssertion();
    }

    private class DuplicateAssertion {

        public void forImages(Image a, Image b) {
            var aName = a.path().getFileName().toString();
            var bName = b.path().getFileName().toString();
            expectLog()
                    .toContain("Collision %s".formatted(aName))
                    .toContain("vs [%s]".formatted(bName));
        }

    }

    private static class StubDirectoryOpener implements DirectoryOpener {

        private final Queue<Path> openedDirectories = new LinkedList<>();

        @Override
        public void open(Path directory) throws UnsupportedOperationException {
            openedDirectories.add(directory);
        }

        public void clear() {
            openedDirectories.clear();
        }

    }

    private static class StubImageProvider implements ImageProvider {

        private final List<Image> images = new ArrayList<>();

        public void addImage(Image image) {
            images.add(image);
        }

        public void clear() {
            images.clear();
        }

        @Override
        public Image[] imagesAt(Path directory) {
            return images.toArray(new Image[0]);
        }

    }

    private record StubPathProvider(Path parentDirectory) implements PathProvider {

        @Override
        public Path root() {
            return parentDirectory.resolve("bin");
        }

        @Override
        public Path currentBin() {
            return root().resolve("current");
        }
    }

}
