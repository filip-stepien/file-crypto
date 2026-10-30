package xyz.cursedman.filecrypto.benchmark;

import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;

public record BenchmarkResult(
    CryptorAlgorithm algorithm,
    String fileName,
    long fileSize,
    double encryptMillis,
    double decryptMillis,
    long outputSize,
    boolean valid
) {

    public static final double BYTES_PER_MB = 1024.0 * 1024;

    public static double throughput(long bytes, double millis) {
        return millis > 0 ? (bytes / BYTES_PER_MB) / (millis / 1000.0) : 0;
    }

    public double encryptThroughput() {
        return throughput(fileSize, encryptMillis);
    }

    public double decryptThroughput() {
        return throughput(fileSize, decryptMillis);
    }

    public long overheadBytes() {
        return outputSize - fileSize;
    }

    public double overheadPercent() {
        return fileSize > 0 ? overheadBytes() * 100.0 / fileSize : 0;
    }
}
