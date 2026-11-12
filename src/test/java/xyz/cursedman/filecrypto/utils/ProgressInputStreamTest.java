package xyz.cursedman.filecrypto.utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgressInputStreamTest {

    @Test
    void countsBytesAndReportsEnd() throws IOException {
        int size = 10 * 1024 * 1024;
        List<Long> reports = new ArrayList<>();

        try (InputStream in = new ProgressInputStream(new ByteArrayInputStream(new byte[size]), size, reports::add)) {
            byte[] buffer = new byte[190];
            while (in.read(buffer) != -1) {
                // drain
            }
        }

        assertEquals(size, reports.getLast());
        assertTrue(reports.size() <= 1001, "too many reports: " + reports.size());
        assertTrue(reports.size() > 100, "too few reports: " + reports.size());
    }

    @Test
    void interruptedThreadStopsReading() {
        InputStream in = new ProgressInputStream(new ByteArrayInputStream(new byte[1024]), 1024, done -> {});

        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedIOException.class, () -> in.read(new byte[16]));
        } finally {
            Thread.interrupted(); // clear flag for other tests
        }
    }
}
