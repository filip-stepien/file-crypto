package xyz.cursedman.filecrypto.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public class PathInputController {

    public enum PathType {
        FILE,
        DIRECTORY
    }

    @FXML
    private TextField pathTextField;

    @FXML
    private Label pathLabel;

    @Getter
    @Setter
    private PathType pathType = PathType.DIRECTORY;

    @Getter
    private Path path = null;

    @Setter
    private Consumer<Path> onPathSelected;

    @FXML
    private void choosePath() {
        Stage stage = (Stage) pathTextField.getScene().getWindow();
        File selected;

        if (pathType == PathType.FILE) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select file");
            selected = fileChooser.showOpenDialog(stage);
        } else {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select directory");
            selected = directoryChooser.showDialog(stage);
        }

        if (selected == null) {
            this.path = null;
            return;
        }

        Path path = selected.toPath();
        pathTextField.setText(path.toString());
        this.path = path;

        if (onPathSelected != null) {
            onPathSelected.accept(path);
        }
    }

    public void setLabel(String text) {
        pathLabel.setText(text);
    }

    public void setPath(Path path) {
        this.path = path;
        pathTextField.setText(path.toString());
    }
}
