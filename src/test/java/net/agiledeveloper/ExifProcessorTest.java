package net.agiledeveloper;

import net.agiledeveloper.image.processors.ExifImageProcessor;
import org.junit.jupiter.api.Disabled;

@Disabled("implement ExifImageProcessor")
class ExifProcessorTest extends ImageProcessorTest {

    public ExifProcessorTest() {
        super(new ExifImageProcessor());
    }

}

