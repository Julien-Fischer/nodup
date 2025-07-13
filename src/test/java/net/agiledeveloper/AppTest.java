package net.agiledeveloper;

import net.agiledeveloper.image.Image;
import net.agiledeveloper.image.ImageProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static net.agiledeveloper.stubs.StubImage.ImageBuilder.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

class AppTest {

    @TempDir
    private Path tempDir;
    private Path directory;

    private TestHandler handler;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    public StubImageProvider imageProvider = new StubImageProvider();


    @BeforeEach
    void setUp() {
        mockStdout();
        mockLogger();
        imageProvider.clear();
        App.imageProvider = imageProvider;
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }


    @Test
    void it_throws_no_exception() {
        assertThatNoException()
                .isThrownBy(App::new);
    }

    @Test
    void without_parameters_fails() throws IOException {
        havingDirectoryNamed("directory");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(this::startingAppWithoutParameters)
                .withMessageContaining("Missing required arguments");
    }

    @Test
    void with_no_images() throws IOException {
        havingDirectoryNamed("directory")
                .empty();

        whenStartingAppWithParameters(directory.toString());

        assertThatNoFilesWereFound();
    }

    @Test
    void with_images_but_no_collision() throws IOException {
        havingDirectoryNamed("directory")
                .containing(aBigDog(), aDog(), aCat());

        whenStartingAppWithParameters(directory.toString());

        assertThatFilesWereFound(3);
        assertThatNoDuplicatesWereFound();
    }

    @Test
    void with_collisions() throws IOException {
        var a = aDogImage().named("dog-a").build();
        var b = aDogImage().named("dog-b").build();
        havingDirectoryNamed("directory")
                .containing(aBigDog(), a, b, aCat());

        whenStartingAppWithParameters(directory.toString());

        assertThatDuplicatesWereFound(1)
                .forImages(a, b);
    }


    private void startingAppWithoutParameters() {
        whenStartingAppWithParameters();
    }

    private void whenStartingAppWithParameters(String... parameters) {
        App.main(parameters);
    }

    private FilePrecondition havingDirectoryNamed(String directory) throws IOException {
        this.directory = tempDir.resolve(directory);
        Files.createDirectory(this.directory);
        return new FilePrecondition(imageProvider, this.directory);
    }

    private void assertThatFilesWereFound(int count) {
        assertThatLogContains("Found %s images in %s".formatted(count, this.directory));
    }

    private void assertThatNoFilesWereFound() {
        assertThatFilesWereFound(0);
    }

    private void assertThatNoDuplicatesWereFound() {
        assertThatLogContains("Found 0 collisions");
    }

    private void assertThatStdoutContains(String expected) {
        try {
            assertThat(outputStream.toString()).contains(expected);
        } finally {
            System.setOut(originalOut);
        }
    }

    private void assertThatLogContains(String expected) {
        try {
            assertThat(handler.getMessages()).contains(expected);
        } finally {
            System.setOut(originalOut);
        }
    }

    private void mockLogger() {
        var logger = Logger.getLogger(App.class.getSimpleName());
        handler = new TestHandler();
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
    }

    private void mockStdout() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }


    static class TestHandler extends Handler {

        private String messages = "";

        @Override
        public void publish(LogRecord record) {
            messages += record.getMessage() + " | ";
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

        public String getMessages() {
            return messages;
        }
    }

    private record FilePrecondition(StubImageProvider imageProvider, Path directory) {

        public void empty() {}

        public void containing(Image... images) throws IOException {
            for (var image : images) {
                imageProvider.addImage(image);
                Path fileToCreate = directory.resolve(image.path());
                Files.createDirectories(fileToCreate.getParent());
                Files.createFile(fileToCreate);
            }
        }

    }

    private DuplicateAssertion assertThatDuplicatesWereFound(int count) {
        assertThatLogContains("Found %d collisions".formatted(count));
        return new DuplicateAssertion();
    }

    private class DuplicateAssertion {

        public void forImages(Image a, Image b) {
            var aName = a.path().getFileName().toString();
            var bName = b.path().getFileName().toString();
            assertThatLogContains("Collision %s".formatted(aName));
            assertThatLogContains("vs %s".formatted(bName));
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

}
