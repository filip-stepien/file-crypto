package xyz.cursedman.filecrypto.benchmark;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestFileSizesTest {

    @Test
    void parsesDefault() {
        assertEquals(List.of(1, 5, 10, 25, 50), TestFileSizes.parse(TestFileSizes.DEFAULT));
    }

    @Test
    void acceptsMixedSeparatorsAndSortsUniqueValues() {
        assertEquals(List.of(2, 8, 100), TestFileSizes.parse(" 100;8  2, 8 "));
    }

    @Test
    void rejectsBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> TestFileSizes.parse("  "));
    }

    @Test
    void rejectsNonNumbers() {
        assertThrows(IllegalArgumentException.class, () -> TestFileSizes.parse("1, abc"));
        assertThrows(IllegalArgumentException.class, () -> TestFileSizes.parse("1.5"));
    }

    @Test
    void rejectsSizesOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> TestFileSizes.parse("0"));
        assertThrows(IllegalArgumentException.class, () -> TestFileSizes.parse("-5"));
        assertThrows(IllegalArgumentException.class, () -> TestFileSizes.parse(String.valueOf(TestFileSizes.MAX_SIZE_MB + 1)));
    }
}
