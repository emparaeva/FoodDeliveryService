package com.example.fooddeliverysystem2.client.controller;

import com.example.fooddeliverysystem2.client.utils.AlertUtil;
import com.example.fooddeliverysystem2.client.utils.HttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        // Подключаем CSS
        try {
            String cssPath = "com/example/fooddeliverysystem2/client/view/styles.css";
            if (getClass().getClassLoader().getResourceAsStream(cssPath) == null) {
                System.out.println("LoginController: CSS не найден: " + cssPath);
            } else {
                System.out.println("LoginController: CSS найден: " + cssPath);
            }
            if (usernameField != null) {
                usernameField.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        try {
                            String cssUrl = getClass().getClassLoader().getResource(cssPath) != null
                                    ? getClass().getClassLoader().getResource(cssPath).toExternalForm() : null;
                            if (cssUrl != null) {
                                newScene.getStylesheets().add(cssUrl);
                                System.out.println("LoginController: CSS успешно добавлен: " + cssUrl);
                            } else {
                                System.out.println("LoginController: CSS не добавлен: " + cssPath);
                            }
                        } catch (Exception e) {
                            System.out.println("LoginController: Исключение при добавлении CSS: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("LoginController: Сцена не готова");
                    }
                });
            } else {
                System.out.println("LoginController: usernameField is null");
            }
        } catch (Exception e) {
            System.out.println("LoginController: Ошибка инициализации CSS: " + e.getMessage());
            e.printStackTrace();
        }
        loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Введите имя пользователя и пароль");
            return;
        }

        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);
        System.out.println("Sending login request: " + loginData);

        Task<Map<String, Object>> loginTask = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                String response = HttpClientUtil.sendPostRequest("/auth/login", loginData);
                System.out.println("Login response: " + response);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response, Map.class);
            }
        };

        loginTask.setOnSucceeded(event -> {
            Map<String, Object> userData = loginTask.getValue();
            System.out.println("Parsed userData: " + userData);
            Platform.runLater(() -> {
                try {
                    // Извлекаем role.name
                    @SuppressWarnings("unchecked")
                    Map<String, Object> roleMap = (Map<String, Object>) userData.get("role");
                    String role = (String) roleMap.get("name");

                    String fxmlFile;
                    switch (role) {
                        case "CLIENT":
                            fxmlFile = "/com/example/fooddeliverysystem2/client/view/client_main.fxml";
                            break;
                        case "MANAGER":
                            fxmlFile = "/com/example/fooddeliverysystem2/client/view/manager_main.fxml";
                            break;
                        case "COURIER":
                            fxmlFile = "/com/example/fooddeliverysystem2/client/view/courier_main.fxml";
                            break;
                        case "ADMIN":
                            fxmlFile = "/com/example/fooddeliverysystem2/client/view/admin_main.fxml";
                            break;
                        default:
                            AlertUtil.showError("Неизвестная роль");
                            return;
                    }

                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                    Parent root = loader.load();

                    // Передаём userData в контроллер
                    Object controller = loader.getController();
                    controller.getClass().getMethod("setUserData", Map.class).invoke(controller, userData);

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Food Delivery System - " + role);
                    stage.show();

                } catch (Exception e) {
                    AlertUtil.showError("Неверный логин или пароль");
                }
            });
        });

        loginTask.setOnFailed(event -> Platform.runLater(() -> {
            AlertUtil.showError("Неверный логин или пароль");
        }));

        new Thread(loginTask).start();
    }
}