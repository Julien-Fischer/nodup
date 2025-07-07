package net.agiledeveloper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

class AppTest {

    @Test
    void name() {
        assertThatNoException()
                .isThrownBy(App::new);
    }

}
