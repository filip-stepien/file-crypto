package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EncryptionSettingsController {

    @FXML
    private PasswordInputController passwordInputController;

    @FXML
    private AlgorithmSettingsController algorithmSettingsController;

}
