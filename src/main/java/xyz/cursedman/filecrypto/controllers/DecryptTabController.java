package xyz.cursedman.filecrypto.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.wrappers.HeaderCryptor;
import xyz.cursedman.filecrypto.cryptors.wrappers.ZipFileCryptor;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.utils.FileSizeFormatter;
import xyz.cursedman.filecrypto.utils.Popup;
import xyz.cursedman.filecrypto.utils.ProgressInputStream;
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
        Task<Void> task = getDecryptionTask(inputPath, outputPath, keyString);
        Thread thread = new Thread(task);
        thread.setDaemon(true);

        progressBarController.bind(task);
        progressBarController.showCancel(event -> task.cancel());
        progressBarController.setButtonDisabled(true);
        loadingOverlayController.setVisible(true);

        thread.start();
    }

    private void endTask() {
        progressBarController.unbind();
        progressBarController.hideTaskButtons();
        progressBarController.setButtonDisabled(false);
        progressBarController.setLoading(false);
        loadingOverlayController.setVisible(false);
    }

    private Task<Void> getDecryptionTask(String inputPath, String outputPath, String keyString) {
        Stopwatch stopwatch = new Stopwatch();

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                stopwatch.start();

                updateMessage("Decrypting...");
                long totalBytes = Files.size(Path.of(inputPath));

                try (InputStream inputStream = new ProgressInputStream(
                    Files.newInputStream(Path.of(inputPath)),
                    totalBytes,
                    done -> {
                        updateProgress(done, totalBytes);
                        updateMessage(done < totalBytes
                            ? "Decrypting " + (totalBytes > 0 ? done * 100 / totalBytes : 100) + "%"
                            : "Extracting files...");
                    }
                )) {
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
            protected void cancelled() {
                super.cancelled();
                endTask();
                progressBarController.setLabel("Cancelled");
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                endTask();
                progressBarController.setLabel("");

                Popup.finished(
                    outputPath,
                    stopwatch.getElapsedMillis(),
                    FileSizeFormatter.directorySize(outputPath)
                );
            }

            @Override
            protected void failed() {
                super.failed();
                endTask();
                progressBarController.setLabel("Failed");

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
