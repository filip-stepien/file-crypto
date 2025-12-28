package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class LoadingOverlayController {

    @FXML
    private StackPane loadingOverlay;

    public void setVisible(boolean visible) {
        loadingOverlay.setVisible(visible);
    }
}
