package xyz.cursedman.filecrypto.utils;

public class TimeFormatter {
    public static String format(long millis) {
        if (millis < 1000) {
            return millis + " ms";
        } else if (millis < 60_000) {
            double seconds = millis / 1000.0;
            return String.format("%.2f s", seconds);
        } else if (millis < 3_600_000) {
            long totalSeconds = millis / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            long totalSeconds = millis / 1000;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
