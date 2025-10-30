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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.*;

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

    private final Set<File> fileSet = new LinkedHashSet<>();

    private final ObservableList<File> fileList = FXCollections.observableArrayList();

    private void initializeTable() {
        fileTableView.setItems(fileList);
        fileTableView.setPlaceholder(new Label("Drop files here or click \"Add items\" to select"));
        fileTableView.setOnDragOver(this::handleDragOver);
        fileTableView.setOnDragDropped(this::handleDragDropped);
        fileTableView.getSortOrder().add(fileIconColumn);
        fileTableView.sort();
    }

    private void initializeTableColumns() {
        fileNameColumn.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().getName())
        );

        fileSizeColumn.setCellValueFactory(
            data -> new SimpleStringProperty(data.getValue().length() + " B")
        );

        fileLastModifiedColumn.setCellValueFactory(
            data -> new SimpleStringProperty(
                new Date(data.getValue().lastModified()).toString()
            )
        );

        fileIconColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );

        fileIconColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
            super.updateItem(file, empty);
            if (empty || file == null) {
                setGraphic(null);
            } else {
                FontIcon icon = new FontIcon(file.isDirectory() ? "fas-folder" : "far-file");
                icon.setIconSize(12);
                setGraphic(icon);
            }
            }
        });

        fileIconColumn.setComparator((f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return 1;
            if (!f1.isDirectory() && f2.isDirectory()) return -1;
            return 0;
        });

        fileIconColumn.setSortType(TableColumn.SortType.DESCENDING);
    }

    private void addFiles(List<File> files) {
        List<File> newFiles = files.stream()
            .filter(fileSet::add)
            .toList();

        if (!newFiles.isEmpty()) {
            fileList.addAll(newFiles);
            fileTableView.sort();
        }
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
            addFiles(db.getFiles());
            success = true;
        }

        event.setDropCompleted(success);
        event.consume();
    }

    @FXML
    private void handleAddFolders() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select folders...");

        File selectedFolder = directoryChooser.showDialog(fileTableView.getScene().getWindow());
        addFiles(List.of(selectedFolder));
    }

    @FXML
    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select files...");

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(fileTableView.getScene().getWindow());
        addFiles(selectedFiles);
    }

    @FXML
    public void initialize() {
        initializeTable();
        initializeTableColumns();
    }
}
