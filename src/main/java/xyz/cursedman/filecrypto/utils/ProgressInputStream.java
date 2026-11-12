package xyz.cursedman.filecrypto.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.function.LongConsumer;

/*
 * Counts bytes read (reporting progress) and stops reading once the thread is interrupted
 * (Task.cancel / Future.cancel), so any Cryptor reading from it becomes cancellable.
 */
public class ProgressInputStream extends FilterInputStream {

    private static final long MIN_REPORT_STEP = 64 * 1024;

    private static final long MAX_REPORTS = 1000;

    private final LongConsumer onBytes;

    private final long reportStep;

    private long bytesRead;

    private long nextReport;

    public ProgressInputStream(InputStream in, long totalBytes, LongConsumer onBytes) {
        super(in);
        this.onBytes = onBytes;
        this.reportStep = Math.max(MIN_REPORT_STEP, totalBytes / MAX_REPORTS);
        this.nextReport = reportStep;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    private void checkInterrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("Operation cancelled");
        }
    }

    private void count(long count) {
        if (count < 0) {
            onBytes.accept(bytesRead);
            return;
        }

        bytesRead += count;

        if (bytesRead >= nextReport) {
            nextReport = bytesRead + reportStep;
            onBytes.accept(bytesRead);
        }
    }

    @Override
    public int read() throws IOException {
        checkInterrupted();
        int value = super.read();
        count(value < 0 ? -1 : 1);
        return value;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        checkInterrupted();
        int read = super.read(buffer, offset, length);
        count(read);
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        checkInterrupted();
        long skipped = super.skip(n);
        count(skipped);
        return skipped;
    }
}
