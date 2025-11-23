package xyz.cursedman.filecrypto.controls;

import javafx.scene.Node;
import javafx.scene.control.Spinner;

public class NumberControl implements FieldControl {
    private final Spinner<Integer> spinner = new Spinner<>(0, Integer.MAX_VALUE, 0);

    public NumberControl(String defaultValue, String placeholder) {
        spinner.getValueFactory().setValue(Integer.parseInt(defaultValue));
        spinner.setPromptText(placeholder);
        spinner.setEditable(true);
    }

    @Override
    public Node getNode() {
        return spinner;
    }

    @Override
    public String getControlValue() {
        return spinner.getValue().toString();
    }
}
