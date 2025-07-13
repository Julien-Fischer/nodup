package net.agiledeveloper;

import net.agiledeveloper.image.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
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


    @BeforeEach
    void setUp() {
        mockStdout();
        mockLogger();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }


    @Test
    void name() {
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

        assertThatNoFilesWereFound();
    }

    @Disabled("implement")
    @Test
    void with_collisions() throws IOException {
        havingDirectoryNamed("directory")
                .containing(aBigDog(), aDog(), aDog(), aCat());

        whenStartingAppWithParameters(directory.toString());

        assertThatFilesWereFound(aDog());
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
        return new FilePrecondition(this.directory);
    }

    private void assertThatNoFilesWereFound() {
        assertThatLogContains("Found 0 collisions");
    }

    private void assertThatFilesWereFound(Image... images) {
        assertThatLogContains("Found %s collisions".formatted(images.length));
        // TODO: assert that contains image names
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

        private String messages;

        @Override
        public void publish(LogRecord record) {
            messages += record.getMessage();
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

        public String getMessages() {
            return messages;
        }
    }

    private record FilePrecondition(Path directory) {

        public void empty() { }

        public void containing(Image... images) throws IOException {
            for (var image : images) {
                Path fileToCreate = directory.resolve(image.path());
                Files.createDirectories(fileToCreate.getParent());
                Files.createFile(fileToCreate);
            }
        }

    }

}
