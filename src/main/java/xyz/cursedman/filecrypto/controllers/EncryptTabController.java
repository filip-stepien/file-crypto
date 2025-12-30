package xyz.cursedman.filecrypto.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import xyz.cursedman.filecrypto.App;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.cryptors.HeaderCryptor;
import xyz.cursedman.filecrypto.cryptors.ZipFileCryptor;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.utils.FileSizeFormatter;
import xyz.cursedman.filecrypto.utils.Popup;
import xyz.cursedman.filecrypto.utils.Stopwatch;

import java.io.*;
import java.nio.file.Path;

public class EncryptTabController {

    @FXML
    private ProgressBarController progressBarController;

    @FXML
    private FileTableController fileTableController;

    @FXML
    private EncryptionSettingsController encryptionSettingsController;

    @FXML
    public LoadingOverlayController loadingOverlayController;

    private Task<Void> getEncryptionTask(ZipFileCryptor zipCryptor, String outputFileName) {
        Stopwatch stopwatch = new Stopwatch();

        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                stopwatch.start();
                zipCryptor.createEncryptedZip(fileTableController.getFiles(), outputFileName);
                stopwatch.stop();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                progressBarController.setLoading(false);
                loadingOverlayController.setVisible(false);

                Popup.finished(
                    outputFileName,
                    stopwatch.getElapsedMillis(),
                    FileSizeFormatter.fileSize(outputFileName)
                );

                App.reload();
            }

            @Override
            protected void failed() {
                super.failed();
                progressBarController.setLoading(false);
                loadingOverlayController.setVisible(false);

                Popup.error(
                    "Encryption error",
                    "An error occurred while encrypting files",
                    new VBox(
                        5,
                        new Label("Error log:"),
                        new Label(getException().getMessage()) {{
                            setStyle(
                                "-fx-font-family: 'Monospaced';" +
                                "-fx-background-color: #f0f0f0;" +
                                "-fx-border-color: #cccccc;" +
                                "-fx-padding: 4 6 4 6;"
                            );
                        }}
                    )
                );
            }
        };
    }

    private void createEncryptedOutputFile() {
        if (fileTableController.getFiles().isEmpty()) {
            return;
        }

        Path outputPath = encryptionSettingsController.getPathInputController().getPath();
        String fileName = encryptionSettingsController.getFileNameInputController().getFileName();

        if (outputPath == null) {
            Popup.error(
                "Output path missing",
                "Output path is missing",
                "Please select a directory to save the encrypted file."
            );
            return;
        }

        if (fileName == null || fileName.isBlank()) {
            Popup.error(
                "File name missing",
                "File name is missing",
                "Please enter a valid name for the output file."
            );
            return;
        }

        String fileExtension = ".enc";
        String outputFileName = outputPath.resolve(fileName + fileExtension).toString();

        AlgorithmSettingsController algorithmSettingsController =
            encryptionSettingsController.getAlgorithmSettingsController();

        KeyCreator keyCreator = algorithmSettingsController.getKeyCreator();
        CryptorKey key = keyCreator.createKey(algorithmSettingsController.getFieldValues());
        ZipFileCryptor zipCryptor = new ZipFileCryptor(new HeaderCryptor(keyCreator.createCryptor(key)));
        Thread thread = new Thread(getEncryptionTask(zipCryptor, outputFileName));

        thread.setDaemon(true);
        thread.start();

        progressBarController.setLoading(true);
        loadingOverlayController.setVisible(true);
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
