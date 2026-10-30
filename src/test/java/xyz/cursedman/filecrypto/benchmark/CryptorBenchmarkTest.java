package xyz.cursedman.filecrypto.benchmark;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        List<Integer> progress = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        List<BenchmarkResult> results = new CryptorBenchmark(1).run(
            List.of(file.toFile(), subdirectory.toFile()),
            algorithms,
            (done, total, message) -> {
                assertEquals(6, total);
                progress.add(done);
                messages.add(message);
            }
        );

        assertEquals(2, results.size());
        assertEquals(List.of(0, 0, 1, 2, 3, 4, 5, 6), progress);
        assertEquals("XOR Cipher · a.bin · encrypting 1/1", messages.get(2));
        assertEquals("AES-GCM · a.bin · decrypting 1/1", messages.get(6));
        assertEquals(List.of("a.bin", "a.bin"), results.stream().map(BenchmarkResult::fileName).toList());
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
