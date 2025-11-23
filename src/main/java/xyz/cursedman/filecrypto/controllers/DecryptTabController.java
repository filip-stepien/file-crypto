package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;

public class DecryptTabController {

    @FXML
    private DecryptionSettingsController decryptionSettingsController;

    @FXML
    private ProgressBarController progressBarController;

    @FXML
    void initialize() {
        progressBarController.setButtonText("Decrypt");
    }
}
