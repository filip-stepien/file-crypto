package xyz.cursedman.filecrypto.benchmark;

import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CryptorBenchmark {

    private static final int WARMUP_BYTES = 1024 * 1024;

    private static final int OUTPUT_SLACK_BYTES = 1024;

    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int done, int total, String message);
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
        List<File> regularFiles = files.stream().filter(File::isFile).toList();
        List<BenchmarkResult> results = new ArrayList<>();
        int total = regularFiles.size() * algorithms.size() * stepsPerMeasurement();
        int[] done = {0};

        for (File file : regularFiles) {
            listener.onProgress(done[0], total, "Reading " + file.getName() + "...");
            byte[] data = Files.readAllBytes(file.toPath());

            for (CryptorAlgorithm algorithm : algorithms) {
                String prefix = algorithm.getName() + " · " + file.getName() + " · ";
                results.add(measure(algorithm, file.getName(), data, step ->
                    listener.onProgress(done[0]++, total, prefix + step)
                ));
            }
        }

        listener.onProgress(total, total, "Finished: " + results.size() + " measurements");
        return results;
    }

    public BenchmarkResult measure(CryptorAlgorithm algorithm, String name, byte[] data) throws IOException {
        return measure(algorithm, name, data, step -> {});
    }

    private int stepsPerMeasurement() {
        return 1 + 2 * repetitions;
    }

    private BenchmarkResult measure(
        CryptorAlgorithm algorithm,
        String name,
        byte[] data,
        Consumer<String> onStep
    ) throws IOException {
        onStep.accept("warm-up");
        Cryptor cryptor = createCryptor(algorithm);
        byte[] warmupData = Arrays.copyOf(data, Math.min(data.length, WARMUP_BYTES));
        decrypt(cryptor, encrypt(cryptor, warmupData), warmupData.length);

        double[] encryptTimes = new double[repetitions];
        double[] decryptTimes = new double[repetitions];
        byte[] encrypted = null;
        byte[] decrypted = null;

        for (int i = 0; i < repetitions; i++) {
            String repetition = " " + (i + 1) + "/" + repetitions;

            onStep.accept("encrypting" + repetition);
            long start = System.nanoTime();
            encrypted = encrypt(cryptor, data);
            encryptTimes[i] = elapsedMillis(start);

            onStep.accept("decrypting" + repetition);
            start = System.nanoTime();
            decrypted = decrypt(cryptor, encrypted, data.length);
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

    private static byte[] encrypt(Cryptor cryptor, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + OUTPUT_SLACK_BYTES);
        cryptor.encrypt(new ByteArrayInputStream(data), out);
        return out.toByteArray();
    }

    private static byte[] decrypt(Cryptor cryptor, byte[] data, int expectedSize) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(expectedSize + OUTPUT_SLACK_BYTES);
        cryptor.decrypt(new ByteArrayInputStream(data), out);
        return out.toByteArray();
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
