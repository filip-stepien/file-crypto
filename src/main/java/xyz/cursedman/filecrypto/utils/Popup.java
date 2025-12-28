package xyz.cursedman.filecrypto.utils;

import javafx.scene.Node;
import javafx.scene.control.Alert;

public class Popup {
    static private void createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    static private void createAlert(Alert.AlertType type, String title, String header, Node content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    static public void error(String title, String header, String content) {
        createAlert(Alert.AlertType.ERROR, title, header, content);
    }

    static public void error(String title, String header, Node content) {
        createAlert(Alert.AlertType.ERROR, title, header, content);
    }

    static public void info(String title, String header, String content) {
        createAlert(Alert.AlertType.INFORMATION, title, header, content);
    }

    static public void info(String title, String header, Node content) {
        createAlert(Alert.AlertType.INFORMATION, title, header, content);
    }
}
