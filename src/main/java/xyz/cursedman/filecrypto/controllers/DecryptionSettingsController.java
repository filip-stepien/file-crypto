package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class DecryptionSettingsController {

    @FXML
    private PasswordInputController passwordInputController;

    @FXML
    private TextField encryptedFilePathField;

    @FXML
    private TextField outputFilePathField;

    public String getEncryptedFilePath() {
        return encryptedFilePathField.getText();
    }

    public String getOutputFilePath() {
        return outputFilePathField.getText();
    }

    public String getPassword() {
        return passwordInputController.getPassword();
    }
}
