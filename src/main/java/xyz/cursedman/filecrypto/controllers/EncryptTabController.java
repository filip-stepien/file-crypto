package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;

public class EncryptTabController {

    @FXML
    private ProgressBarController progressBarController;

    @FXML
    private FileTableController fileTableController;

    @FXML
    private EncryptionSettingsController encryptionSettingsController;

    @FXML
    public void initialize() {
        progressBarController.setButtonText("Encrypt");
    }
}
