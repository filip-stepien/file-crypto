package xyz.cursedman.filecrypto.utils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import xyz.cursedman.filecrypto.App;

import java.awt.*;
import java.io.File;
import java.io.IOException;

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

    static public void finished(String filePath, long timeTakenMs, long fileSizeBytes) {
        VBox popupContent = new VBox(
            5,
            new VBox(
                5,
                new Label("Time taken:") {{ setStyle("-fx-font-weight: bold;"); }},
                new Label(TimeFormatter.format(timeTakenMs))
            ),
            new VBox(
                5,
                new Label("Output size:") {{ setStyle("-fx-font-weight: bold;"); }},
                new Label(FileSizeFormatter.format(fileSizeBytes))
            ),
            new VBox(
                5,
                new Label("Output path:") {{ setStyle("-fx-font-weight: bold;"); }},
                new Hyperlink(filePath) {{
                    setStyle("-fx-padding: 0; -fx-border-width: 0;");
                    setOnAction(e -> {
                        try {
                            Desktop.getDesktop().open(new File(filePath).getParentFile());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }}
            )
        );

        Popup.info(
            "Task completed",
            "Operation completed successfully",
            popupContent
        );
    }
}
