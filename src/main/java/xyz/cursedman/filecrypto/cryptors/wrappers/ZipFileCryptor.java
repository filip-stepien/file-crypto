package xyz.cursedman.filecrypto.cryptors.wrappers;

import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.exceptions.InvalidKeyException;

import xyz.cursedman.filecrypto.utils.ProgressInputStream;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.LongConsumer;
import java.util.zip.*;

public class ZipFileCryptor {

    /*
     * Encryption progress: bytes of the packed archive processed so far and its total size
     */
    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(long doneBytes, long totalBytes);
    }

    private final Cryptor cryptor;

    private final ProgressListener progressListener;

    public ZipFileCryptor(Cryptor cryptor) {
        this(cryptor, (done, total) -> {});
    }

    public ZipFileCryptor(Cryptor cryptor, ProgressListener progressListener) {
        this.cryptor = cryptor;
        this.progressListener = progressListener;
    }

    private static void checkInterrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("Operation cancelled");
        }
    }

    public void createEncryptedZip(Collection<File> files, String outputFilePath) throws IOException {
        File tempZip = File.createTempFile("temp", ".enc");

        try (
            FileOutputStream tempFileOutStream = new FileOutputStream(tempZip);
            ZipOutputStream tempZipOutStream = new ZipOutputStream(tempFileOutStream)
        ) {
            for (File file : files) {
                checkInterrupted();
                ZipEntry zipEntry = new ZipEntry(file.getName());
                tempZipOutStream.putNextEntry(zipEntry);

                try (InputStream fis = new ProgressInputStream(new FileInputStream(file), file.length(), done -> {})) {
                    fis.transferTo(tempZipOutStream);
                }

                tempZipOutStream.closeEntry();
            }
        } catch (IOException | RuntimeException e) {
            tempZip.delete();
            throw e;
        }

        long totalBytes = tempZip.length();
        LongConsumer onBytes = done -> progressListener.onProgress(done, totalBytes);
        boolean completed = false;

        try (
            InputStream fileInStream = new ProgressInputStream(new FileInputStream(tempZip), totalBytes, onBytes);
            FileOutputStream fileOutStream = new FileOutputStream(outputFilePath)
        ) {
            cryptor.encrypt(fileInStream, fileOutStream);
            completed = true;
        } finally {
            tempZip.delete();

            // do not leave a half-written file behind after cancel or error
            if (!completed) {
                new File(outputFilePath).delete();
            }
        }
    }

    public void extractEncryptedZip(InputStream encryptedStream, String outputDirPath) throws IOException {
        File tempZip = File.createTempFile("temp", ".zip");

        try (FileOutputStream tempOut = new FileOutputStream(tempZip)) {
            cryptor.decrypt(encryptedStream, tempOut);
        } catch (IOException | RuntimeException e) {
            tempZip.delete();
            throw e;
        }

        try (ZipFile zipFile = new ZipFile(tempZip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File outFile = new File(outputDirPath, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }

                outFile.getParentFile().mkdirs();

                try (
                    InputStream zipInStream = zipFile.getInputStream(entry);
                    FileOutputStream fileOutStream = new FileOutputStream(outFile))
                {
                    zipInStream.transferTo(fileOutStream);
                }
            }
        } catch (ZipException e) {
            throw new InvalidKeyException("Invalid decryption key or corrupted ZIP", e);
        } finally {
            tempZip.delete();
        }
    }
}


