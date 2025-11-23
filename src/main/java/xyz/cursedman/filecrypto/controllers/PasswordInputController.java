package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PasswordInputController {
    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button showPasswordButton;

    @FXML
    private void initialize() {
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility() {
        if (passwordTextField.isVisible()) {
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);

            showPasswordButton.setText("Show");
        } else {
            passwordField.setVisible(false);
            passwordField.setManaged(false);

            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            showPasswordButton.setText("Hide");
        }
    }

    public String getPassword() {
        return passwordTextField.getText();
    }
}
