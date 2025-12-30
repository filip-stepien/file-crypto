package xyz.cursedman.filecrypto.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.HeaderCryptor;
import xyz.cursedman.filecrypto.cryptors.ZipFileCryptor;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.utils.FileSizeFormatter;
import xyz.cursedman.filecrypto.utils.Popup;
import xyz.cursedman.filecrypto.utils.Stopwatch;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DecryptTabController {

    @FXML
    private DecryptionSettingsController decryptionSettingsController;

    @FXML
    private ProgressBarController progressBarController;

    @FXML
    public LoadingOverlayController loadingOverlayController;

    private void createDecryptedOutputFile() {
        String inputPath = decryptionSettingsController.getInputFilePathController().getPath().toString();
        String outputPath = decryptionSettingsController.getOutputFilePathController().getPath().toString();
        String keyString = decryptionSettingsController.getKeyInput().getText();
        Thread thread = new Thread(getDecryptionTask(inputPath, outputPath, keyString));

        thread.setDaemon(true);
        thread.start();

        progressBarController.setLoading(true);
        loadingOverlayController.setVisible(true);
    }

    private Task<Void> getDecryptionTask(String inputPath, String outputPath, String keyString) {
        Stopwatch stopwatch = new Stopwatch();

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                stopwatch.start();

                try (InputStream inputStream = Files.newInputStream(Path.of(inputPath))) {
                    HeaderCryptor.Header header = HeaderCryptor.readHeader(inputStream);
                    CryptorAlgorithm algorithm = CryptorAlgorithm.fromAlgorithmName(header.algorithmName());
                    KeyCreator keyCreator = CryptorAlgorithm.getKeyCreator(algorithm);
                    Cryptor cryptor = keyCreator.createCryptor(keyCreator.createKey(keyString));
                    ZipFileCryptor zipCryptor = new ZipFileCryptor(cryptor);

                    zipCryptor.extractEncryptedZip(inputStream, outputPath);
                }

                stopwatch.stop();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                progressBarController.setLoading(false);
                loadingOverlayController.setVisible(false);

                Popup.finished(
                    outputPath,
                    stopwatch.getElapsedMillis(),
                    FileSizeFormatter.directorySize(outputPath)
                );
            }

            @Override
            protected void failed() {
                super.failed();
                progressBarController.setLoading(false);
                loadingOverlayController.setVisible(false);

                Popup.error(
                    "Decryption error",
                    "An error occurred while decrypting files",
                    "Provided key is invalid."
                );
            }
        };
    }

    @FXML
    void initialize() {
        progressBarController.setButtonText("Decrypt");
        progressBarController.setOnButtonClick(event -> createDecryptedOutputFile());
        decryptionSettingsController.getInputFilePathController().setPathType(PathInputController.PathType.FILE);
    }
}
