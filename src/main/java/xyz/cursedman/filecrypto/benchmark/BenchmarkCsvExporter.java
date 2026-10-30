package xyz.cursedman.filecrypto.benchmark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenchmarkCsvExporter {

    private static final String HEADER = String.join(",",
        "algorithm",
        "file",
        "file_size_bytes",
        "encrypt_ms",
        "decrypt_ms",
        "encrypt_mb_s",
        "decrypt_mb_s",
        "output_size_bytes",
        "overhead_bytes",
        "overhead_percent",
        "valid"
    );

    public static void export(Path path, List<BenchmarkResult> results) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (BenchmarkResult result : results) {
            lines.add(String.format(
                Locale.ROOT,
                "%s,%s,%d,%.4f,%.4f,%.2f,%.2f,%d,%d,%.4f,%b",
                result.algorithm().getName(),
                escape(result.fileName()),
                result.fileSize(),
                result.encryptMillis(),
                result.decryptMillis(),
                result.encryptThroughput(),
                result.decryptThroughput(),
                result.outputSize(),
                result.overheadBytes(),
                result.overheadPercent(),
                result.valid()
            ));
        }

        Files.write(path, lines);
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
