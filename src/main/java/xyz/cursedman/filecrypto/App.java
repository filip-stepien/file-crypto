package xyz.cursedman.filecrypto;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import xyz.cursedman.filecrypto.controllers.FileTableController;

import java.io.IOException;

public class App extends Application {

    @FXML
    private FileTableController fileTableController;

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