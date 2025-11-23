package xyz.cursedman.filecrypto.controls;

import javafx.scene.control.Control;
import javafx.scene.control.TextField;

public class TextControl implements FieldControl {
    private final TextField textField = new TextField();

    @Override
    public Control getControl() {
        return textField;
    }

    @Override
    public String getControlValue() {
        return textField.getText();
    }
}
