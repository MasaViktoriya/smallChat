package ru.masaviktoria.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainClient extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();
        stage.setTitle("Чат");
        stage.setScene(new Scene(root, 400, 400));
        stage.show();
        ChatController chatController = loader.getController();
        stage.setOnCloseRequest(windowEvent -> {
            chatController.close();
            Platform.exit();
            System.exit(0);
        });
    }
}
