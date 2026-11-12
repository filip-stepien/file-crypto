package xyz.cursedman.filecrypto.controls;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

import java.util.concurrent.atomic.AtomicReference;

/*
 * Progress bar with a centered label. update() may be called from any thread;
 * updates are coalesced into one FX-thread change, like Task.updateProgress.
 */
public class LabeledProgressBar extends StackPane {

    private record State(double progress, String text) {}

    private final ProgressBar progressBar = new ProgressBar(0);

    private final Label label = new Label();

    private final AtomicReference<State> pending = new AtomicReference<>();

    public LabeledProgressBar() {
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(20);
        getChildren().addAll(progressBar, label);
        setShown(false);
    }

    public void update(double progress, String text) {
        if (pending.getAndSet(new State(progress, text)) == null) {
            Platform.runLater(() -> {
                State state = pending.getAndSet(null);
                if (state != null && isVisible()) {
                    progressBar.setProgress(state.progress());
                    label.setText(state.text());
                }
            });
        }
    }

    /*
     * Shows (reset to empty) or hides the bar; must be called on the FX thread
     */
    public void setShown(boolean shown) {
        pending.set(null);
        progressBar.setProgress(0);
        label.setText("");
        setVisible(shown);
        setManaged(shown);
    }
}
