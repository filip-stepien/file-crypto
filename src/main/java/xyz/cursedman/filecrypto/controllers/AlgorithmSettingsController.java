package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import xyz.cursedman.filecrypto.controls.FieldControl;
import xyz.cursedman.filecrypto.controls.NumberControl;
import xyz.cursedman.filecrypto.controls.TextControl;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.keys.KeyInputType;

import java.util.*;

public class AlgorithmSettingsController {

    @FXML
    private VBox settingsContainer;

    private final Map<String, FieldControl> currentControls = new HashMap<>();

    private FieldControl resolveControlType(KeyInputType type) {
        return switch (type) {
            case TEXT -> new TextControl();
            case NUMBER -> new NumberControl();
            default -> throw new RuntimeException("Unsupported key input type type: " + type);
        };
    }

    private void createControl(KeyInputField field) {
        FieldControl control = resolveControlType(field.getType());
        settingsContainer.getChildren().add(control.getControl());
        currentControls.put(field.getId(), control);
    }

    private void clearControls() {
        settingsContainer.getChildren().clear();
        currentControls.clear();
    }

    public void setKeyCreator(KeyCreator keyCreator) {
        clearControls();

        for (KeyInputField field : keyCreator.getKeyInputFields()) {
            createControl(field);
        }
    }

    public Map<String, String> getFieldValues() {
        Map<String, String> fieldValues = new HashMap<>();

        for (var control : currentControls.entrySet()) {
            fieldValues.put(control.getKey(), control.getValue().getControlValue());
        }

        return fieldValues;
    }
}