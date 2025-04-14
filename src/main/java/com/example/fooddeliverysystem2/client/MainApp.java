package com.example.fooddeliverysystem2.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        String fxmlPath = "/com/example/fooddeliverysystem2/client/view/login.fxml";
        System.out.println("Loading FXML: " + fxmlPath); // Для отладки
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        if (fxmlLoader.getLocation() == null) {
            throw new IOException("Cannot find login.fxml at " + fxmlPath);
        }
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        primaryStage.setTitle("Food Delivery System - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
