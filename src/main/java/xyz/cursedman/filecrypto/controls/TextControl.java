package xyz.cursedman.filecrypto.controls;

import javafx.scene.Node;
import javafx.scene.control.TextField;

public class TextControl implements FieldControl {
    private final TextField textField = new TextField();

    public TextControl(String defaultValue, String placeholder) {
        textField.setText(defaultValue);
        textField.setPromptText(placeholder);
    }

    @Override
    public Node getNode() {
        return textField;
    }

    @Override
    public String getControlValue() {
        return textField.getText();
    }
}
