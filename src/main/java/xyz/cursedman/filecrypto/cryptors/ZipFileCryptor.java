package xyz.cursedman.filecrypto.cryptors;

import lombok.RequiredArgsConstructor;

import java.io.*;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
        }

//        finally {
//            tempZip.delete();
//        }
    }

    public void extractEncryptedZip(String encryptedZipPath, String outputDirPath) throws IOException {
        File tempZip = File.createTempFile("temp", ".enc.zip");

        try (
            FileInputStream tempFileInStream = new FileInputStream(encryptedZipPath);
            FileOutputStream tempFileOutStream = new FileOutputStream(tempZip)
        ) {
            cryptor.decrypt(tempFileInStream, tempFileOutStream);
        }

        try (
            FileInputStream fileInStream = new FileInputStream(tempZip);
            ZipInputStream zipInStream = new ZipInputStream(fileInStream)
        ) {
            ZipEntry entry;
            while ((entry = zipInStream.getNextEntry()) != null) {
                File outFile = new File(outputDirPath, entry.getName());
                outFile.getParentFile().mkdirs();

                try (FileOutputStream fileOutStream = new FileOutputStream(outFile)) {
                    zipInStream.transferTo(fileOutStream);
                }

                zipInStream.closeEntry();
            }
        }

//        finally {
//            tempZip.delete();
//        }
    }
}


