package xyz.cursedman.filecrypto.benchmark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CryptorBenchmarkTest {

    private static byte[] randomBytes(int size) {
        byte[] data = new byte[size];
        new Random(42).nextBytes(data);
        return data;
    }

    @Test
    void measureProducesValidResultForEveryAlgorithm() throws Exception {
        byte[] data = randomBytes(64 * 1024);
        CryptorBenchmark benchmark = new CryptorBenchmark(2);

        for (CryptorAlgorithm algorithm : CryptorAlgorithm.values()) {
            BenchmarkResult result = benchmark.measure(algorithm, "data.bin", data);

            assertTrue(result.valid(), algorithm.getName());
            assertEquals(algorithm, result.algorithm());
            assertEquals(data.length, result.fileSize());
            assertTrue(result.outputSize() >= data.length, algorithm.getName());
            assertTrue(result.encryptMillis() > 0, algorithm.getName());
            assertTrue(result.decryptMillis() > 0, algorithm.getName());
        }
    }

    @Test
    void runSkipsDirectoriesAndReportsProgress(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("a.bin");
        Files.write(file, randomBytes(4096));
        Path subdirectory = Files.createDirectory(directory.resolve("sub"));
        List<CryptorAlgorithm> algorithms = List.of(CryptorAlgorithm.XOR, CryptorAlgorithm.AES_GCM);
        List<Long> progress = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        // per algorithm: 1 repetition of encrypt + decrypt, 4096 B each
        long expectedTotal = 2 * 2 * 4096;

        List<String> passes = new ArrayList<>();
        List<String> measurements = new ArrayList<>();

        List<BenchmarkResult> results = new CryptorBenchmark(1).run(
            List.of(file.toFile(), subdirectory.toFile()),
            algorithms,
            new CryptorBenchmark.ProgressListener() {
                @Override
                public void onProgress(long done, long total, String message) {
                    assertEquals(expectedTotal, total);
                    progress.add(done);
                    messages.add(message);
                }

                @Override
                public void onMeasurement(double fraction, String message) {
                    assertTrue(fraction >= 0 && fraction <= 1, "fraction " + fraction);
                    measurements.add(message + " " + Math.round(fraction * 100));
                }

                @Override
                public void onPass(double fraction, String operation) {
                    assertTrue(fraction >= 0 && fraction <= 1, "fraction " + fraction);
                    passes.add(operation + " " + Math.round(fraction * 100));
                }
            }
        );

        assertEquals(2, results.size());
        assertEquals(List.of("a.bin", "a.bin"), results.stream().map(BenchmarkResult::fileName).toList());

        for (int i = 1; i < progress.size(); i++) {
            assertTrue(progress.get(i) >= progress.get(i - 1), "progress must not go back: " + progress);
        }
        assertEquals(expectedTotal, progress.getLast());
        assertTrue(messages.contains("Measurement 1/2"), messages.toString());
        assertTrue(messages.contains("Measurement 2/2"), messages.toString());
        assertTrue(measurements.contains("XOR Cipher · a.bin · repetition 1/1 0"), measurements.toString());
        assertTrue(measurements.contains("AES-GCM · a.bin · repetition 1/1 100"), measurements.toString());
        assertTrue(passes.contains("Encrypting 0"), passes.toString());
        assertTrue(passes.contains("Decrypting 100"), passes.toString());
    }

    @Test
    void skipSkipsOnlyCurrentMeasurement(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("a.bin");
        Files.write(file, randomBytes(4096));
        List<CryptorAlgorithm> algorithms = List.of(CryptorAlgorithm.RSA_OAEP, CryptorAlgorithm.AES_GCM);
        List<Long> progress = new ArrayList<>();
        AtomicInteger started = new AtomicInteger();

        BenchmarkRun run = new CryptorBenchmark(1).run(
            List.of(file.toFile()),
            algorithms,
            (done, total, message) -> progress.add(done),
            future -> {
                // skip the first (RSA-OAEP) measurement right away
                if (started.getAndIncrement() == 0) {
                    future.cancel(true);
                }
            }
        );

        assertFalse(run.cancelled());
        assertEquals(List.of(CryptorAlgorithm.AES_GCM), run.results().stream().map(BenchmarkResult::algorithm).toList());
        assertTrue(run.results().getFirst().valid());
        assertEquals(2L * 2 * 4096, progress.getLast());
    }

    @Test
    void interruptCancelsRunAndKeepsCompletedResults(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("a.bin");
        Files.write(file, randomBytes(64 * 1024));
        List<CryptorAlgorithm> algorithms = List.of(CryptorAlgorithm.AES_GCM, CryptorAlgorithm.RSA_OAEP);
        AtomicInteger started = new AtomicInteger();
        AtomicReference<BenchmarkRun> result = new AtomicReference<>();
        CountDownLatch secondStarted = new CountDownLatch(1);

        Thread runner = new Thread(() -> {
            try {
                result.set(new CryptorBenchmark(50).run(
                    List.of(file.toFile()),
                    algorithms,
                    (done, total, message) -> {},
                    future -> {
                        if (started.incrementAndGet() == 2) {
                            secondStarted.countDown();
                        }
                    }
                ));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        runner.start();
        assertTrue(secondStarted.await(30, TimeUnit.SECONDS));
        runner.interrupt(); // what Task.cancel() does
        runner.join(10_000);

        BenchmarkRun run = result.get();
        assertTrue(run.cancelled());
        assertEquals(List.of(CryptorAlgorithm.AES_GCM), run.results().stream().map(BenchmarkResult::algorithm).toList());
    }

    @Test
    void rejectsNonPositiveRepetitions() {
        assertThrows(IllegalArgumentException.class, () -> new CryptorBenchmark(0));
    }

    @Test
    void csvExportWritesHeaderAndRows(@TempDir Path directory) throws Exception {
        Path csv = directory.resolve("out.csv");
        BenchmarkResult result = new BenchmarkResult(
            CryptorAlgorithm.AES_GCM, "x,y.bin", 1024 * 1024, 10, 20, 1024 * 1024 + 28, true
        );

        BenchmarkCsvExporter.export(csv, List.of(result));

        List<String> lines = Files.readAllLines(csv);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).startsWith("algorithm,file,"));
        assertEquals("AES-GCM,\"x,y.bin\",1048576,10.0000,20.0000,100.00,50.00,1048604,28,0.0027,true", lines.get(1));
    }
}
