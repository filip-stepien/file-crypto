package xyz.cursedman.filecrypto.controls;

import javafx.scene.control.Control;
import javafx.scene.control.Spinner;

public class NumberControl implements FieldControl {
    private final Spinner<Integer> spinner = new Spinner<>(0, Integer.MAX_VALUE, 0);

    public NumberControl() {
        spinner.setEditable(true);
    }

    @Override
    public Control getControl() {
        return spinner;
    }

    @Override
    public String getControlValue() {
        return spinner.getValue().toString();
    }
}
