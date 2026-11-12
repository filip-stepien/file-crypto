package xyz.cursedman.filecrypto.controls;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;

import java.util.List;

public class SelectionControl implements FieldControl {
    private final ComboBox<String> comboBox = new ComboBox<>();

    public SelectionControl(List<String> options, String defaultValue, String placeholder) {
        comboBox.getItems().setAll(options);
        comboBox.setPromptText(placeholder);
        comboBox.setValue(defaultValue);
    }

    @Override
    public Node getNode() {
        return comboBox;
    }

    @Override
    public String getControlValue() {
        return comboBox.getValue();
    }

    @Override
    public void setControlValue(String value) {
        comboBox.setValue(value);
    }
}
