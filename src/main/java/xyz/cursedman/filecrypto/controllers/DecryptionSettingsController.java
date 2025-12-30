package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.Getter;

@Getter
public class DecryptionSettingsController {

    @FXML
    private TextField keyInput;

    @FXML
    private PathInputController inputFilePathController;

    @FXML
    private PathInputController outputFilePathController;

    @FXML
    void initialize() {
        outputFilePathController.setLabel("Output path");
        inputFilePathController.setLabel("Encrypted file path");
        inputFilePathController.setOnPathSelected(path -> {
            outputFilePathController.setPath(path.getParent().resolve("decrypted"));
        });
    }
}
