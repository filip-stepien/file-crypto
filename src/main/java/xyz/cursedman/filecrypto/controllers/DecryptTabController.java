package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.HeaderCryptor;
import xyz.cursedman.filecrypto.keys.KeyCreator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecryptTabController {

    @FXML
    private DecryptionSettingsController decryptionSettingsController;

    @FXML
    private ProgressBarController progressBarController;

    private void createDecryptedOutputFile() {
        Path inputPath = decryptionSettingsController.getInputFilePathController().getPath();
        Path outputPath = decryptionSettingsController.getOutputFilePathController().getPath();
        String keyString = decryptionSettingsController.getKeyInput().getText();

        try (
            InputStream inputStream = Files.newInputStream(inputPath);
            OutputStream outputStream = Files.newOutputStream(outputPath)
        ) {
            HeaderCryptor.Header header = HeaderCryptor.readHeader(inputStream);
            CryptorAlgorithm algorithm = CryptorAlgorithm.fromAlgorithmName(header.algorithmName());
            KeyCreator keyCreator = CryptorAlgorithm.getKeyCreator(algorithm);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void initialize() {
        progressBarController.setButtonText("Decrypt");
        progressBarController.setOnButtonClick(event -> createDecryptedOutputFile());
    }
}
