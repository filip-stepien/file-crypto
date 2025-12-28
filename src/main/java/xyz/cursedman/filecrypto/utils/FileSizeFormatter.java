package xyz.cursedman.filecrypto.utils;

public class FileSizeFormatter {
    public static String format(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024L * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024L * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
