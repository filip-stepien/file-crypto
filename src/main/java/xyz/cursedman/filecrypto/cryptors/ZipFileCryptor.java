package xyz.cursedman.filecrypto.cryptors;

import xyz.cursedman.filecrypto.exceptions.InvalidKeyException;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.*;

public class ZipFileCryptor {

    private final Cryptor cryptor;

    public ZipFileCryptor(Cryptor cryptor) {
        this.cryptor = cryptor;
    }

    public void createEncryptedZip(Collection<File> files, String outputFilePath) throws IOException {
        File tempZip = File.createTempFile("temp", ".enc");

        try (
            FileOutputStream tempFileOutStream = new FileOutputStream(tempZip);
            ZipOutputStream tempZipOutStream = new ZipOutputStream(tempFileOutStream)
        ) {
            for (File file : files) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                tempZipOutStream.putNextEntry(zipEntry);

                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.transferTo(tempZipOutStream);
                }

                tempZipOutStream.closeEntry();
            }
        }

        try (
            FileInputStream fileInStream = new FileInputStream(tempZip);
            FileOutputStream fileOutStream = new FileOutputStream(outputFilePath)
        ) {
            cryptor.encrypt(fileInStream, fileOutStream);
        } finally {
            tempZip.delete();
        }
    }

    public void extractEncryptedZip(InputStream encryptedStream, String outputDirPath) throws IOException {
        File tempZip = File.createTempFile("temp", ".zip");

        try (FileOutputStream tempOut = new FileOutputStream(tempZip)) {
            cryptor.decrypt(encryptedStream, tempOut);
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


