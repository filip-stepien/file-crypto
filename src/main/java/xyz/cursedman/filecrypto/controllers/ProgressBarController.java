package xyz.cursedman.filecrypto.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;

public class ProgressBarController {

    @FXML
    private Label progressBarLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button button;

    public void setButtonText(String text) {
        button.setText(text);
    }

    public void setProgress(double value) {
        progressBar.setProgress(value);
    }

    public void setProgressBarLabel(String text) {
        progressBarLabel.setText(text);
    }

    public void setOnButtonClick(EventHandler<ActionEvent> handler) {
        button.setOnAction(handler);
    }
}
