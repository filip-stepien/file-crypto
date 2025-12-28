package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EncryptionSettingsController {

    @FXML
    private AlgorithmSettingsController algorithmSettingsController;

    @FXML
    private PathInputController pathInputController;

    @FXML
    private FileNameInputController fileNameInputController;
}
