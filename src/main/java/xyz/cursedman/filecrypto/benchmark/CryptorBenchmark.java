package xyz.cursedman.filecrypto.benchmark;

import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.utils.ProgressInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CryptorBenchmark {

    private static final int OUTPUT_SLACK_BYTES = 1024;

    @FunctionalInterface
    public interface ProgressListener {
        /*
         * Whole run: bytes processed so far, total bytes, message ("Measurement 3/14", "Reading ...")
         */
        void onProgress(long doneBytes, long totalBytes, String message);

        /*
         * Current measurement: fraction 0..1 of its repetitions and "<algorithm> · <file> · repetition 2/3"
         */
        default void onMeasurement(double fraction, String message) {}

        /*
         * Current pass within a repetition: fraction 0..1 and operation ("Encrypting" / "Decrypting")
         */
        default void onPass(double fraction, String operation) {}
    }

    /*
     * Reports progress of a single measurement: bytes processed within it, repetition (1-based),
     * current operation and its fraction
     */
    @FunctionalInterface
    private interface MeasurementProgress {
        void onProgress(long measurementBytes, int repetition, String operation, double passFraction);
    }

    private final int repetitions;

    public CryptorBenchmark(int repetitions) {
        if (repetitions < 1) {
            throw new IllegalArgumentException("Repetitions must be at least 1");
        }

        this.repetitions = repetitions;
    }

    public List<BenchmarkResult> run(
        Collection<File> files,
        Collection<CryptorAlgorithm> algorithms,
        ProgressListener listener
    ) throws IOException {
        return run(files, algorithms, listener, future -> {}).results();
    }

    /*
     * Every measurement runs as its own Future (passed to onMeasurementStarted):
     * cancelling it skips that measurement, interrupting the calling thread cancels the whole run.
     */
    public BenchmarkRun run(
        Collection<File> files,
        Collection<CryptorAlgorithm> algorithms,
        ProgressListener listener,
        Consumer<Future<?>> onMeasurementStarted
    ) throws IOException {
        List<File> regularFiles = files.stream().filter(File::isFile).toList();
        List<BenchmarkResult> results = new ArrayList<>();

        long total = 0;
        for (File file : regularFiles) {
            total += measurementBytes(file.length()) * algorithms.size();
        }

        long totalBytes = total;
        long done = 0;
        int measurementCount = regularFiles.size() * algorithms.size();
        AtomicInteger currentMeasurement = new AtomicInteger();
        ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "benchmark-measurement");
            thread.setDaemon(true);
            return thread;
        });

        try {
            for (File file : regularFiles) {
                listener.onProgress(done, totalBytes, "Reading " + file.getName() + "...");
                listener.onMeasurement(0, "Reading " + file.getName() + "...");
                listener.onPass(0, "");
                byte[] data;

                try {
                    data = Files.readAllBytes(file.toPath());
                } catch (ClosedByInterruptException e) {
                    return new BenchmarkRun(results, true);
                }

                for (CryptorAlgorithm algorithm : algorithms) {
                    String label = algorithm.getName() + " · " + file.getName();
                    int measurementId = currentMeasurement.incrementAndGet();
                    String measurementMessage = "Measurement " + measurementId + "/" + measurementCount;
                    long measurementBytes = measurementBytes(data.length);
                    long base = done;

                    Future<BenchmarkResult> future = executor.submit(() ->
                        measure(algorithm, file.getName(), data, (bytes, repetition, operation, passFraction) -> {
                            // ignore late reports from an already skipped measurement
                            if (currentMeasurement.get() == measurementId) {
                                listener.onProgress(base + bytes, totalBytes, measurementMessage);
                                listener.onMeasurement(
                                    measurementBytes > 0 ? (double) bytes / measurementBytes : 1,
                                    label + " · repetition " + repetition + "/" + repetitions
                                );
                                listener.onPass(passFraction, operation);
                            }
                        })
                    );
                    onMeasurementStarted.accept(future);

                    try {
                        results.add(future.get());
                    } catch (CancellationException e) {
                        // skipped: the measurement was interrupted, nothing to record
                    } catch (InterruptedException e) {
                        future.cancel(true);
                        return new BenchmarkRun(results, true);
                    } catch (ExecutionException e) {
                        throw unwrap(e);
                    }

                    done = base + measurementBytes;
                }
            }
        } finally {
            executor.shutdownNow();
        }

        listener.onProgress(totalBytes, totalBytes, "Finished: " + results.size() + " measurements");
        return new BenchmarkRun(results, false);
    }

    public BenchmarkResult measure(CryptorAlgorithm algorithm, String name, byte[] data) throws IOException {
        return measure(algorithm, name, data, (bytes, repetition, operation, passFraction) -> {});
    }

    /*
     * Progress units of one measurement: encrypt + decrypt per repetition
     */
    private long measurementBytes(long size) {
        return 2L * repetitions * size;
    }

    private BenchmarkResult measure(
        CryptorAlgorithm algorithm,
        String name,
        byte[] data,
        MeasurementProgress progress
    ) throws IOException {
        long[] offset = {0};

        Cryptor cryptor = createCryptor(algorithm);
        double[] encryptTimes = new double[repetitions];
        double[] decryptTimes = new double[repetitions];
        byte[] encrypted = null;
        byte[] decrypted = null;

        for (int i = 0; i < repetitions; i++) {
            int repetition = i + 1;

            long start = System.nanoTime();
            encrypted = encrypt(cryptor, data, offset, progress, repetition);
            encryptTimes[i] = elapsedMillis(start);

            start = System.nanoTime();
            decrypted = decrypt(cryptor, encrypted, data.length, offset, progress, repetition);
            decryptTimes[i] = elapsedMillis(start);
        }

        return new BenchmarkResult(
            algorithm,
            name,
            data.length,
            median(encryptTimes),
            median(decryptTimes),
            encrypted.length,
            Arrays.equals(data, decrypted)
        );
    }

    private static Cryptor createCryptor(CryptorAlgorithm algorithm) {
        KeyCreator keyCreator = CryptorAlgorithm.getKeyCreator(algorithm);
        Map<String, String> defaults = keyCreator.getKeyGeneratorFields().stream()
            .collect(Collectors.toMap(KeyInputField::getId, KeyInputField::getDefaultValue));

        return keyCreator.createCryptor(keyCreator.generateCryptorKey(defaults));
    }

    /*
     * Wraps input so the pass reports progress (scaled to plaintextSize units) and stops when interrupted
     */
    private static ProgressInputStream track(
        byte[] input,
        long plaintextSize,
        long[] offset,
        MeasurementProgress progress,
        int repetition,
        String operation
    ) {
        long base = offset[0];
        offset[0] += plaintextSize;
        progress.onProgress(base, repetition, operation, 0);

        return new ProgressInputStream(new ByteArrayInputStream(input), input.length, bytesRead -> {
            long scaled = input.length > 0 ? bytesRead * plaintextSize / input.length : 0;
            double fraction = input.length > 0 ? (double) bytesRead / input.length : 1;
            progress.onProgress(base + scaled, repetition, operation, fraction);
        });
    }

    private static byte[] encrypt(
        Cryptor cryptor, byte[] data, long[] offset, MeasurementProgress progress, int repetition
    ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + OUTPUT_SLACK_BYTES);
        cryptor.encrypt(track(data, data.length, offset, progress, repetition, "Encrypting"), out);
        return out.toByteArray();
    }

    private static byte[] decrypt(
        Cryptor cryptor, byte[] data, int expectedSize, long[] offset, MeasurementProgress progress, int repetition
    ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(expectedSize + OUTPUT_SLACK_BYTES);
        cryptor.decrypt(track(data, expectedSize, offset, progress, repetition, "Decrypting"), out);
        return out.toByteArray();
    }

    private static IOException unwrap(ExecutionException e) {
        Throwable cause = e.getCause();

        if (cause instanceof IOException ioException) {
            return ioException;
        }
        if (cause instanceof UncheckedIOException unchecked) {
            return unchecked.getCause();
        }
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        return new IOException(cause);
    }

    private static double elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }

    private static double median(double[] values) {
        double[] sorted = values.clone();
        Arrays.sort(sorted);
        int middle = sorted.length / 2;

        return sorted.length % 2 == 0
            ? (sorted[middle - 1] + sorted[middle]) / 2
            : sorted[middle];
    }
}
