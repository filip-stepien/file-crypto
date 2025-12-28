package xyz.cursedman.filecrypto.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class FileTableController {

    @FXML
    private TableView<File> fileTableView;

    @FXML
    private Button removeButton;

    @FXML
    private Label itemsCounter;

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

    private void setItemCount() {
        int itemsCount = fileSet.size();
        int selectedItemsCount = fileTableView.getSelectionModel().getSelectedItems().size();

        String itemsCountText = itemsCount + (itemsCount == 1 ? " item" : " items");
        String selectedItemsCountText =
            selectedItemsCount > 0 ? "(" + selectedItemsCount + " selected)" : "";

        itemsCounter.setText(itemsCountText + " " + selectedItemsCountText);
    }

    private void clearSelection() {
        fileTableView.getSelectionModel().clearSelection();
        setItemCount();
    }

    private void addFiles(List<File> files) {
        List<File> newFiles = files.stream()
            .filter(fileSet::add)
            .toList();

        if (!newFiles.isEmpty()) {
            fileList.addAll(newFiles);
            fileTableView.sort();
            setItemCount();
        }
    }

    private void removeFiles(List<File> files) {
        files.forEach(fileSet::remove);
        fileList.removeAll(files);
        clearSelection();
    }

    private void clearFiles() {
        fileList.clear();
        fileSet.clear();
        setItemCount();
    }

    private void initializeTable() {
        fileTableView.setItems(fileList);
        fileTableView.setPlaceholder(new Label("Drop files here or click \"Add items\" to select"));

        fileTableView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();

            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        fileTableView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                addFiles(db.getFiles());
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });

        fileTableView.setRowFactory(tableView -> {
            TableRow<File> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    clearSelection();
                }
            });

            return row ;
        });

        fileTableView.focusedProperty().addListener(
            (observable, isFocused, wasFocused) -> {
                if (!isFocused) {
                    clearSelection();
                }
        });

        fileTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileTableView.getSelectionModel()
            .getSelectedItems()
            .addListener((ListChangeListener<File>) c -> setItemCount());

        fileTableView.getSortOrder().add(fileIconColumn);
        fileTableView.sort();

        removeButton.setFocusTraversable(false);
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

    public Collection<File> getFiles() {
        return fileList;
    }

    @FXML
    private void initialize() {
        initializeTable();
        initializeTableColumns();
    }

    @FXML
    private void handleAddFolders() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select folders...");

        File selectedFolder = directoryChooser.showDialog(fileTableView.getScene().getWindow());

        if (selectedFolder != null) {
            addFiles(List.of(selectedFolder));
        }
    }

    @FXML
    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select files...");

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(fileTableView.getScene().getWindow());

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            addFiles(selectedFiles);
        }
    }

    @FXML
    private void handleRemoveSelectedItems() {
        List<File> selected = fileTableView.getSelectionModel().getSelectedItems();
        removeFiles(selected);
    }

    @FXML
    private void handleRemoveAllItems() {
        clearFiles();
    }
}
