package net.agiledeveloper;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.ImageDeduplicator;
import net.agiledeveloper.image.ImageProvider;
import net.agiledeveloper.image.bin.Bin;
import net.agiledeveloper.image.processors.BruteForceProcessor;
import net.agiledeveloper.image.processors.ExifProcessor;
import net.agiledeveloper.image.processors.collision.PixelCollisionDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.lang.String.join;
import static net.agiledeveloper.stubs.StubImage.ImageBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    private StubBin bin;
    private Orchestrator orchestrator;

    private final List<Class<?>> loggersToMock = List.of(
            App.class,
            Orchestrator.class,
            ImageDeduplicator.class,
            BruteForceProcessor.class
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


    @Test
    void it_uses_the_current_directory_by_default() throws IOException {
        havingDirectoryNamed("directory");

        whenStartingApp()
                .withoutAnyParameter();

        expectLog()
                .toContain(System.getProperty("user.dir"));
    }

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
                .withParameters(directoryToScan.toString());

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
                .withParameters(directoryToScan.toString());

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
    void it_uses_default_log_level() throws IOException {
        havingDirectoryNamed("directory");

        whenStartingApp()
                .withParameters(directoryToScan.toString());

        expectLog()
                .toContain("Log level: INFO");
    }

    @Test
    void it_uses_specified_log_level() throws IOException {
        havingDirectoryNamed("directory");

        whenStartingApp()
                .withParameters(directoryToScan.toString(), "--log=fine");

        expectLog()
                .toContain("Log level: FINE");
    }

    @Test
    void it_uses_specified_log_level_when_no_positional_parameters() throws IOException {
        havingDirectoryNamed("directory");

        whenStartingApp()
                .withParameters("--log=fine");

        expectLog()
                .toContain(System.getProperty("user.dir"))
                .toContain("Log level: FINE");
    }

    @Test
    void it_prints_an_help_message() {
        whenStartingApp()
                .withParameters("--help");

        expectStdout()
                .toContain("nodup [/path/to/dir] [OPTIONS]")
                .toContain("Positional parameters:")
                .toContain("$1               (Optional) The path to the directory to process")
                .toContain("Options:")
                .toContain("--log            Set the logging level (e.g., severe, warning, info, fine, finer, finest).")
                .toContain("Flags:");
    }

    @Test
    void unknown_arguments_throw() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> whenStartingApp().withParameters("--unknown-argument"))
                .withMessageContaining("Unknown argument: --unknown-argument");
    }

    @Test
    void it_opens_bin_directory() {
        whenStartingApp()
                .withParameters("--bin");

        expect(directoryOpener)
                .toHaveOpened(bin.root());
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
        TextFile textFile = aTextFile("a/text/file.txt");
        havingDirectoryNamed("directory")
                .containing(textFile);
        String filePath = textFile.fullyQualifiedPath().toString();

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> whenStartingApp().withParameters(filePath))
                .withMessageContaining("Specified path is not a directory: " + filePath);
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
                .toContain("Found %s images in %s".formatted(count, directoryToScan));
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
        bin = new StubBin(tempDir);
        var imageProcessor = new ExifProcessor(new PixelCollisionDetector());
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
                .toContain("About to [COPY] %d duplicates to %s".formatted(count, bin.path()));
        return new CopyAssertion();
    }

    private class CopyAssertion {

        public void forImages(Image... images) {
            expectLog()
                    .toContain("Done [COPY] %s duplicates to %s".formatted(images.length, bin.path()));
        }

    }

    private MoveAssertion assertThatDuplicatesWereMoved(int count) {
        expectLog()
                .toContain("About to [MOVE] %d duplicates to %s".formatted(count, bin.path()));
        return new MoveAssertion();
    }

    private class MoveAssertion {

        public void forImages(Image... images) {
            expectLog()
                    .toContain("Done [MOVE] %s duplicates to %s".formatted(images.length, bin.path()));
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
                    .toContain("vs %s".formatted(bName));
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

    private record StubBin(Path parentDirectory) implements Bin {

        @Override
        public Path root() {
            return Paths.get("bin-directory");
        }

        @Override
        public Path path() {
            Path currentBin = parentDirectory.resolve(root());
            try {
                Files.createDirectories(currentBin.getParent());
                Files.createDirectories(currentBin);
            } catch (IOException cause) {
                throw new Bin.InitializationException("Could not initialize bin: " + cause.getMessage());
            }
            return currentBin;
        }

    }

}
