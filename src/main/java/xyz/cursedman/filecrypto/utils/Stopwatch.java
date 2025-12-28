package xyz.cursedman.filecrypto.utils;

public class Stopwatch {

    private long startTime;

    private long endTime;

    private boolean running = false;

    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }

    public void stop() {
        endTime = System.currentTimeMillis();
        running = false;
    }

    public long getElapsedMillis() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        } else {
            return endTime - startTime;
        }
    }

    public String getFormattedElapsed() {
        return TimeFormatter.format(getElapsedMillis());
    }
}
