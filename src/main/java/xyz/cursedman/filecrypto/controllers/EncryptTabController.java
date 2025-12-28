package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;

import java.io.*;
import java.nio.file.Path;
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


    private void createEncryptedOutputFile() {
        String fileExtension = ".encrypted.zip";
        Path outputPath = encryptionSettingsController.getPathInputController().getPath();
        String fileName = encryptionSettingsController.getFileNameInputController().getFileName();

        if (outputPath == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Output path missing");
            alert.setHeaderText("Output path is missing");
            alert.setContentText("Please select a directory to save the encrypted file.");
            alert.showAndWait();
            return;
        }

        if (fileName == null || fileName.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File name missing");
            alert.setHeaderText("File name is missing");
            alert.setContentText("Please enter a valid name for the output file.");
            alert.showAndWait();
            return;
        }

        String fullOutputFileName = outputPath.resolve(fileName + fileExtension).toString();
        encryptFiles(fullOutputFileName);
    }

    @FXML
    public void initialize() {
        encryptionSettingsController.getPathInputController().setLabel("Output path");
        progressBarController.setButtonText("Encrypt");
        progressBarController.setOnButtonClick(event -> {
            createEncryptedOutputFile();
        });
    }
}
