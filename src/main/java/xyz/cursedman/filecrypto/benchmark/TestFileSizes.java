package xyz.cursedman.filecrypto.benchmark;

import java.util.Arrays;
import java.util.List;

public class TestFileSizes {

    public static final String DEFAULT = "1, 5, 10, 25, 50";

    public static final int MAX_SIZE_MB = 500;

    public static List<Integer> parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Enter at least one file size.");
        }

        return Arrays.stream(text.split("[,;\\s]+"))
            .filter(part -> !part.isEmpty())
            .map(TestFileSizes::parseSize)
            .distinct()
            .sorted()
            .toList();
    }

    private static int parseSize(String part) {
        int size;

        try {
            size = Integer.parseInt(part);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + part + "\" is not a whole number of MB.");
        }

        if (size < 1 || size > MAX_SIZE_MB) {
            throw new IllegalArgumentException(
                "File size must be between 1 and " + MAX_SIZE_MB + " MB, got " + size + "."
            );
        }

        return size;
    }
}
