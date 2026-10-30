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

    public void setLoading(boolean loading) {
        progressBar.setProgress(loading ? ProgressBar.INDETERMINATE_PROGRESS : 0);
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public void setLabel(String text) {
        progressBarLabel.setText(text);
    }

    public void setButtonDisabled(boolean disabled) {
        button.setDisable(disabled);
    }

    public void setOnButtonClick(EventHandler<ActionEvent> handler) {
        button.setOnAction(handler);
    }
}
