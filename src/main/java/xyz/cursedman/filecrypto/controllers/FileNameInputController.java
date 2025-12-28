package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class FileNameInputController {

    @FXML
    private TextField fileName;

    private void restrictFileName(TextField textField) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();

            if (!newText.matches("[a-zA-Z0-9._-]*")) {
                return null;
            }

            return change;
        };

        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    @FXML
    private void initialize() {
        restrictFileName(fileName);
    }

    public String getFileName() {
        return fileName.getText();
    }
}
