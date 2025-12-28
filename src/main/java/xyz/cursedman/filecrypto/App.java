package xyz.cursedman.filecrypto;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import xyz.cursedman.filecrypto.controllers.FileTableController;
import xyz.cursedman.filecrypto.utils.FileSizeFormatter;
import xyz.cursedman.filecrypto.utils.Popup;
import xyz.cursedman.filecrypto.utils.TimeFormatter;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("views/app.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("File cryptor");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}