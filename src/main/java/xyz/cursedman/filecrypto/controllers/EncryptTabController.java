package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;

import java.io.*;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EncryptTabController {

    @FXML
    private ProgressBarController progressBarController;

    @FXML
    private FileTableController fileTableController;

    @FXML
    private EncryptionSettingsController encryptionSettingsController;

    private InputStream zipToInputStream(Collection<File> files, Cryptor cryptor) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (File file : files) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(zipEntry);

                try (InputStream fileInputStream = new FileInputStream(file)) {
                    cryptor.encrypt(fileInputStream, zipOutputStream);
                }

                zipOutputStream.closeEntry();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private void encryptFiles(String filePath) {
        if (fileTableController.getFiles().isEmpty()) {
            return;
        }

        AlgorithmSettingsController algorithmSettingsController =
            encryptionSettingsController.getAlgorithmSettingsController();

        KeyCreator keyCreator = algorithmSettingsController.getKeyCreator();
        CryptorKey key = keyCreator.createKey(algorithmSettingsController.getFieldValues());
        Cryptor cryptor = keyCreator.createCryptor(key);

        try (
            InputStream zipStream = zipToInputStream(fileTableController.getFiles(), cryptor);
            OutputStream out = new FileOutputStream(filePath)
        ) {
            zipStream.transferTo(out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        progressBarController.setButtonText("Encrypt");
        progressBarController.setOnButtonClick(event -> {
            encryptFiles("out.zip");
        });
    }
}
