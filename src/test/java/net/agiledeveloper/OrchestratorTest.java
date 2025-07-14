package net.agiledeveloper;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.ImageDeduplicator;
import net.agiledeveloper.image.ImageProvider;
import net.agiledeveloper.image.bin.Bin;
import net.agiledeveloper.image.processors.BruteForceProcessor;
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
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.lang.String.join;
import static net.agiledeveloper.stubs.StubImage.ImageBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

class OrchestratorTest {

    @TempDir
    private Path tempDir;
    private Path directoryToScan;

    private TestHandler handler;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    private final StubImageProvider imageProvider = new StubImageProvider();
    private StubBin bin = new StubBin(tempDir);

    private static final Orchestrator orchestrator = new Orchestrator();

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
        mockImageProvider();
        mockBin();
    }


    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }


    @Test
    void it_throws_no_exception() {
        assertThatNoException()
                .isThrownBy(Orchestrator::new);
    }

    @Test
    void without_parameters_fails() throws IOException {
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
        var a = aDogImage().located(directoryToScan).named("dog-a").build();
        var b = aDogImage().located(directoryToScan).named("dog-b").build();
        givenThat(directoryToScan)
                .contains(aBigDog(), a, b, aCat());

        whenStartingApp()
                .withParameters(directoryToScan.toString(), "--move");

        assertThatDuplicatesWereMoved(1)
                .forImages(a);
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

    private AppAction whenStartingApp(String... parameters) {
        return new AppAction(parameters);
    }

    private record AppAction(String... parameters) {

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
        return new FilePrecondition(imageProvider, this.directoryToScan);
    }

    private void havingDirectoryToScan(String directory) throws IOException {
        this.directoryToScan = tempDir.resolve(directory);
        Files.createDirectory(this.directoryToScan);
    }

    private void assertThatFilesWereFound(int count) {
        expectLog()
                .toContain("Found %s images in %s".formatted(count, this.directoryToScan));
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

    private void mockBin() {
        bin = new StubBin(tempDir);
        App.bin = bin;
    }

    private void mockImageProvider() {
        imageProvider.clear();
        App.imageProvider = imageProvider;
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

    private static class StubImageProvider implements ImageProvider {

        private final List<Image> images = new ArrayList<>();

        public void addImage(Image image) {
            images.add(image);
        }

        public void clear() {
            images.clear();
        }

        @Override
        public Image[] imagesAt(String directory) {
            return images.toArray(new Image[0]);
        }

    }

    private record StubBin(Path parentDirectory) implements Bin {

        @Override
        public Path path() {
            var rootBinDirectory = Paths.get("bin-directory");
            Path currentBin = parentDirectory.resolve(rootBinDirectory);
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
