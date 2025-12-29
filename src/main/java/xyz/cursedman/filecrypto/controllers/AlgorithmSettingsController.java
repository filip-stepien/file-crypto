package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import xyz.cursedman.filecrypto.controls.FieldControl;
import xyz.cursedman.filecrypto.controls.FieldInfo;
import xyz.cursedman.filecrypto.controls.NumberControl;
import xyz.cursedman.filecrypto.controls.TextControl;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;

import java.util.*;

public class AlgorithmSettingsController {

    @Getter
    private KeyCreator keyCreator = CryptorAlgorithm.getKeyCreator(CryptorAlgorithm.CAESAR);

    @FXML
    private ComboBox<String> algorithmComboBox;

    @FXML
    private VBox settingsContainer;

    private final Map<String, FieldControl> currentControls = new HashMap<>();

    @FXML
    private void initialize() {
        algorithmComboBox.getItems().addAll(
            Arrays.stream(CryptorAlgorithm.values())
                .map(CryptorAlgorithm::getAlgorithmName)
                .toList()
        );

        algorithmComboBox.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, prev, curr) -> {
                selectAlgorithm(CryptorAlgorithm.fromAlgorithmName(curr));
            }
        );

        selectAlgorithm(CryptorAlgorithm.CAESAR);
    }

    private void selectAlgorithm(CryptorAlgorithm algorithm) {
        algorithmComboBox.getSelectionModel().select(algorithm.getAlgorithmName());
        setKeyCreator(CryptorAlgorithm.getKeyCreator(algorithm));
    }

    private FieldInfo resolveField(KeyInputField field) {
        FieldInfo.FieldInfoBuilder fieldInfo = FieldInfo.builder()
            .label(field.getLabel())
            .description(field.getDescription());

        switch (field.getType()) {
            case TEXT, HEX -> fieldInfo.fieldControl(
                new TextControl(field.getDefaultValue(), field.getPlaceholder())
            );
            case NUMBER -> fieldInfo.fieldControl(
                new NumberControl(field.getDefaultValue(), field.getPlaceholder())
            );
            default -> throw new RuntimeException("Unsupported key input type type: " + field.getType());
        }

        return fieldInfo.build();
    }

    private void createControl(KeyInputField field) {
        FieldInfo fieldInfo = resolveField(field);
        settingsContainer.getChildren().add(fieldInfo.getNode());
        currentControls.put(field.getId(), fieldInfo.getFieldControl());
    }

    private void clearControls() {
        settingsContainer.getChildren().clear();
        currentControls.clear();
    }

    private void setKeyCreator(KeyCreator keyCreator) {
        clearControls();

        for (KeyInputField field : keyCreator.getKeyInputFields()) {
            createControl(field);
        }

        this.keyCreator = keyCreator;
    }

    public Map<String, String> getFieldValues() {
        Map<String, String> fieldValues = new HashMap<>();

        for (var control : currentControls.entrySet()) {
            fieldValues.put(control.getKey(), control.getValue().getControlValue());
        }

        return fieldValues;
    }
}