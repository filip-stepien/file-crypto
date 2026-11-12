package xyz.cursedman.filecrypto.controllers;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ProgressBarController {

    @FXML
    private Label progressBarLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button button;

    @FXML
    private Button skipButton;

    @FXML
    private Button cancelButton;

    public void setButtonText(String text) {
        button.setText(text);
    }

    public void setLoading(boolean loading) {
        unbind();
        progressBar.setProgress(loading ? ProgressBar.INDETERMINATE_PROGRESS : 0);
    }

    public void setProgress(double progress) {
        unbind();
        progressBar.setProgress(progress);
    }

    public void setLabel(String text) {
        unbind();
        progressBarLabel.setText(text);
    }

    public void setButtonDisabled(boolean disabled) {
        button.setDisable(disabled);
    }

    public void setOnButtonClick(EventHandler<ActionEvent> handler) {
        button.setOnAction(handler);
    }

    /*
     * Shows the worker's progress and message on the bar while it runs
     */
    public void bind(Worker<?> worker) {
        progressBar.progressProperty().bind(worker.progressProperty());
        progressBarLabel.textProperty().bind(worker.messageProperty());
    }

    public void unbind() {
        progressBar.progressProperty().unbind();
        progressBarLabel.textProperty().unbind();
    }

    public void showCancel(EventHandler<ActionEvent> handler) {
        showTaskButton(cancelButton, handler);
    }

    public void showSkip(EventHandler<ActionEvent> handler) {
        showTaskButton(skipButton, handler);
    }

    public void hideTaskButtons() {
        for (Button taskButton : new Button[]{skipButton, cancelButton}) {
            taskButton.setVisible(false);
            taskButton.setManaged(false);
            taskButton.setOnAction(null);
        }
    }

    private static void showTaskButton(Button taskButton, EventHandler<ActionEvent> handler) {
        taskButton.setOnAction(handler);
        taskButton.setVisible(true);
        taskButton.setManaged(true);
    }
}
