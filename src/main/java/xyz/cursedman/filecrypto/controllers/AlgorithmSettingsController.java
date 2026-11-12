package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.Getter;
import xyz.cursedman.filecrypto.controls.FieldControl;
import xyz.cursedman.filecrypto.controls.FieldInfo;
import xyz.cursedman.filecrypto.controls.NumberControl;
import xyz.cursedman.filecrypto.controls.SelectionControl;
import xyz.cursedman.filecrypto.controls.TextControl;
import xyz.cursedman.filecrypto.cryptors.CryptorAlgorithm;
import xyz.cursedman.filecrypto.cryptors.CryptorKey;
import xyz.cursedman.filecrypto.keys.AsymmetricKeyCreator;
import xyz.cursedman.filecrypto.keys.KeyCreator;
import xyz.cursedman.filecrypto.keys.KeyInputField;
import xyz.cursedman.filecrypto.utils.Popup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class AlgorithmSettingsController {

    @Getter
    private KeyCreator keyCreator = CryptorAlgorithm.getKeyCreator(CryptorAlgorithm.CAESAR);

    @FXML
    private ComboBox<String> algorithmComboBox;

    @FXML
    private VBox settingsContainer;

    private final Map<String, FieldControl> currentControls = new HashMap<>();

    private final Map<String, FieldControl> generatorControls = new HashMap<>();

    @FXML
    private void initialize() {
        algorithmComboBox.getItems().addAll(
            Arrays.stream(CryptorAlgorithm.values())
                .map(CryptorAlgorithm::getName)
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
        algorithmComboBox.getSelectionModel().select(algorithm.getName());
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
            case SELECTION -> fieldInfo.fieldControl(
                new SelectionControl(field.getOptions(), field.getDefaultValue(), field.getPlaceholder())
            );
            default -> throw new RuntimeException("Unsupported key input type type: " + field.getType());
        }

        return fieldInfo.build();
    }

    private void createControl(KeyInputField field, Map<String, FieldControl> controls) {
        FieldInfo fieldInfo = resolveField(field);
        settingsContainer.getChildren().add(fieldInfo.getNode());
        controls.put(field.getId(), fieldInfo.getFieldControl());
    }

    private void clearControls() {
        settingsContainer.getChildren().clear();
        currentControls.clear();
        generatorControls.clear();
    }

    private static Map<String, String> readValues(Map<String, FieldControl> controls) {
        Map<String, String> values = new HashMap<>();

        for (var control : controls.entrySet()) {
            values.put(control.getKey(), control.getValue().getControlValue());
        }

        return values;
    }

    /*
     * Asymmetric algorithms need a key pair: the public part goes to the input fields,
     * the private part is shown so it can be pasted in the Decrypt tab.
     */
    private void createKeyPairGenerator(AsymmetricKeyCreator asymmetricKeyCreator) {
        Label header = new Label("Generate key pair");
        header.setStyle("-fx-font-weight: bold;");
        settingsContainer.getChildren().addAll(new Separator(), header);

        for (KeyInputField field : asymmetricKeyCreator.getKeyGeneratorFields()) {
            createControl(field, generatorControls);
        }

        TextField privateKeyField = new TextField();
        privateKeyField.setEditable(false);
        privateKeyField.setPromptText("Private key appears here after generating");
        HBox.setHgrow(privateKeyField, Priority.ALWAYS);

        Button copyButton = new Button("Copy");
        copyButton.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(privateKeyField.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });

        Button saveButton = new Button("Save...");
        saveButton.setOnAction(event -> savePrivateKey(privateKeyField.getText()));

        copyButton.disableProperty().bind(privateKeyField.textProperty().isEmpty());
        saveButton.disableProperty().bind(privateKeyField.textProperty().isEmpty());

        Button generateButton = new Button("Generate");
        generateButton.setOnAction(event -> {
            CryptorKey key = asymmetricKeyCreator.generateCryptorKey(readValues(generatorControls));

            asymmetricKeyCreator.toEncryptionFieldValues(key).forEach((id, value) -> {
                FieldControl control = currentControls.get(id);
                if (control != null) {
                    control.setControlValue(value);
                }
            });

            privateKeyField.setText(asymmetricKeyCreator.toDecryptionKeyInput(key));
        });

        Label privateKeyLabel = new Label("Private key (paste in Decrypt tab)");
        privateKeyLabel.setStyle("-fx-font-weight: bold;");

        settingsContainer.getChildren().addAll(
            generateButton,
            new VBox(5, privateKeyLabel, new HBox(5, privateKeyField, copyButton, saveButton))
        );
    }

    private void savePrivateKey(String privateKey) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save private key");
        fileChooser.setInitialFileName("private.key");
        File file = fileChooser.showSaveDialog(settingsContainer.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            Files.writeString(file.toPath(), privateKey);
        } catch (IOException e) {
            Popup.error("Save error", "Could not save private key", e.getMessage());
        }
    }

    private void setKeyCreator(KeyCreator keyCreator) {
        clearControls();

        for (KeyInputField field : keyCreator.getKeyInputFields()) {
            createControl(field, currentControls);
        }

        if (keyCreator instanceof AsymmetricKeyCreator asymmetricKeyCreator) {
            createKeyPairGenerator(asymmetricKeyCreator);
        }

        this.keyCreator = keyCreator;
    }

    public Map<String, String> getFieldValues() {
        return readValues(currentControls);
    }
}