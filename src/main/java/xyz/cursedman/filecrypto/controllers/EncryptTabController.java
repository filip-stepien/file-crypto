package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import xyz.cursedman.filecrypto.cryptors.Cryptor;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.utils.FileSizeFormatter;
import xyz.cursedman.filecrypto.utils.Popup;
import xyz.cursedman.filecrypto.utils.Stopwatch;
import xyz.cursedman.filecrypto.utils.TimeFormatter;

import java.awt.Desktop;
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

    private InputStream zipToInputStream(Collection<File> files, Cryptor cryptor) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (File file : files) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOutputStream.putNextEntry(zipEntry);

            try (InputStream fileInputStream = new FileInputStream(file)) {
                cryptor.encrypt(fileInputStream, zipOutputStream);
            }

            zipOutputStream.closeEntry();
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    private void showEncryptionFinishedPopup(String filePath, long timeTakenMs, long fileSizeBytes) {
        VBox popupContent = new VBox(
            5,
            new VBox(
                5,
                new Label("Output file:") {{ setStyle("-fx-font-weight: bold;"); }},
                new Label(TimeFormatter.format(timeTakenMs))
            ),
            new VBox(
                5,
                new Label("File size:") {{ setStyle("-fx-font-weight: bold;"); }},
                new Label(FileSizeFormatter.format(fileSizeBytes))
            ),
            new VBox(
                5,
                new Label("Output path:") {{ setStyle("-fx-font-weight: bold;"); }},
                new Hyperlink(filePath) {{
                    setStyle("-fx-padding: 0; -fx-border-width: 0;");
                    setOnAction(e -> {
                        try {
                            Desktop.getDesktop().open(new File(filePath).getParentFile());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }}
            )
        );

        Popup.info(
            "Encryption completed",
            "Files were encrypted successfully",
            popupContent
        );
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

        String fileExtension = ".encrypted.zip";
        String fullOutputFileName = outputPath.resolve(fileName + fileExtension).toString();

        AlgorithmSettingsController algorithmSettingsController =
            encryptionSettingsController.getAlgorithmSettingsController();

        KeyCreator keyCreator = algorithmSettingsController.getKeyCreator();
        CryptorKey key = keyCreator.createKey(algorithmSettingsController.getFieldValues());
        Cryptor cryptor = keyCreator.createCryptor(key);
        Stopwatch stopwatch = new Stopwatch();

        stopwatch.start();

        try (
            InputStream zipStream = zipToInputStream(fileTableController.getFiles(), cryptor);
            OutputStream out = new FileOutputStream(fullOutputFileName)
        ) {
            zipStream.transferTo(out);
        } catch (IOException e) {
            Popup.error(
                "Encryption error",
                "An error occurred while encrypting files",
                new VBox(
                    5,
                    new Label("Error log:"),
                    new Label(e.getMessage()) {{
                        setStyle(
                            "-fx-font-family: 'Monospaced';" +
                            "-fx-background-color: #f0f0f0;" +
                            "-fx-border-color: #cccccc;" +
                            "-fx-padding: 4 6 4 6;"
                        );
                    }}
                )
            );

            throw new RuntimeException(e);
        }

        stopwatch.stop();

        long timeTakenMs = stopwatch.getElapsedMillis();
        long outputFileSizeBytes = new File(fullOutputFileName).length();

        showEncryptionFinishedPopup(
            fullOutputFileName,
            timeTakenMs,
            outputFileSizeBytes
        );
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
