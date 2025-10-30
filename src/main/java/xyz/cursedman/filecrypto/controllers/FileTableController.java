package xyz.cursedman.filecrypto.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.Date;
import java.util.List;

public class FileTableController {
    @FXML
    private TableView<File> fileTableView;

    @FXML
    private TableColumn<File, String> fileNameColumn;

    @FXML
    private TableColumn<File, String> fileSizeColumn;

    @FXML
    private TableColumn<File, String> fileLastModifiedColumn;

    @FXML
    private TableColumn<File, File> fileIconColumn;

    private final ObservableList<File> fileList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        fileTableView.setItems(fileList);
        fileTableView.setPlaceholder(new Label("Drop files here or click \"Add items\" to select"));

        fileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        fileSizeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().length() + " B"));
        fileLastModifiedColumn.setCellValueFactory(data -> new SimpleStringProperty(new Date(data.getValue().lastModified()).toString()));

        fileIconColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));

        fileIconColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setGraphic(null);
                } else {
                    FontIcon icon = file.isDirectory()
                            ? new FontIcon("fas-folder")
                            : new FontIcon("far-file");
                    icon.setIconSize(12);
                    setGraphic(icon);
                }
            }
        });

        fileTableView.setOnDragOver(this::handleDragOver);
        fileTableView.setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            fileList.addAll(files);
            success = true;
        }

        event.setDropCompleted(success);
        event.consume();
    }
}
