package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.HeaderCryptor;
import xyz.cursedman.filecrypto.cryptors.ZipFileCryptor;
import xyz.cursedman.filecrypto.keys.KeyCreator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecryptTabController {

    @FXML
    private DecryptionSettingsController decryptionSettingsController;

    @FXML
    private ProgressBarController progressBarController;

    private void createDecryptedOutputFile() {
        String inputPath = decryptionSettingsController.getInputFilePathController().getPath().toString();
        String outputPath = decryptionSettingsController.getOutputFilePathController().getPath().toString();
        String keyString = decryptionSettingsController.getKeyInput().getText();

        try (InputStream inputStream = Files.newInputStream(Path.of(inputPath))) {
//            HeaderCryptor.Header header = HeaderCryptor.readHeader(inputStream);
//            CryptorAlgorithm algorithm = CryptorAlgorithm.fromAlgorithmName(header.algorithmName());
            CryptorAlgorithm algorithm = CryptorAlgorithm.CAESAR;
            KeyCreator keyCreator = CryptorAlgorithm.getKeyCreator(algorithm);
            Cryptor cryptor = keyCreator.createCryptor(keyCreator.createKey(keyString));
            ZipFileCryptor zipCryptor = new ZipFileCryptor(cryptor);

            zipCryptor.extractEncryptedZip(inputPath, outputPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void initialize() {
        progressBarController.setButtonText("Decrypt");
        progressBarController.setOnButtonClick(event -> createDecryptedOutputFile());
        decryptionSettingsController.getInputFilePathController().setPathType(PathInputController.PathType.FILE);
    }
}
