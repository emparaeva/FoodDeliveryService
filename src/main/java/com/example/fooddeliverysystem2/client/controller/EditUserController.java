package com.example.fooddeliverysystem2.client.controller;

import com.example.fooddeliverysystem2.client.utils.AlertUtil;
import com.example.fooddeliverysystem2.client.utils.HttpClientUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.Map;

public class EditUserController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private ChoiceBox<Map<String, Object>> roleChoiceBox;
    @FXML
    private TextField passwordField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label errorLabel;

    private Map<String, Object> userData;
    private ObservableList<Map<String, Object>> roles;
    private Runnable onSaveCallback;

    @FXML
    private void initialize() {
        // Подключаем CSS
        try {
            String cssPath = "com/example/fooddeliverysystem2/client/view/styles.css";
            if (getClass().getClassLoader().getResourceAsStream(cssPath) == null) {
                System.out.println("EditUserController: CSS не найден: " + cssPath);
            } else {
                System.out.println("EditUserController: CSS найден: " + cssPath);
            }
            if (usernameField != null) {
                usernameField.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        try {
                            String cssUrl = getClass().getClassLoader().getResource(cssPath) != null
                                    ? getClass().getClassLoader().getResource(cssPath).toExternalForm() : null;
                            if (cssUrl != null) {
                                newScene.getStylesheets().add(cssUrl);
                                System.out.println("EditUserController: CSS успешно добавлен: " + cssUrl);
                            } else {
                                System.out.println("EditUserController: CSS не добавлен: " + cssPath);
                            }
                        } catch (Exception e) {
                            System.out.println("EditUserController: Исключение при добавлении CSS: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("EditUserController: Сцена не готова");
                    }
                });
            } else {
                System.out.println("EditUserController: usernameField is null");
            }
        } catch (Exception e) {
            System.out.println("EditUserController: Ошибка инициализации CSS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUserData(Map<String, Object> userData, ObservableList<Map<String, Object>> roles, Runnable onSaveCallback) {
        this.userData = userData;
        this.roles = roles;
        this.onSaveCallback = onSaveCallback;

        usernameField.setText((String) userData.get("username"));
        fullNameField.setText((String) userData.get("fullName"));
        phoneField.setText((String) userData.get("phone"));

        roleChoiceBox.setItems(roles);
        roleChoiceBox.setConverter(new StringConverter<Map<String, Object>>() {
            @Override
            public String toString(Map<String, Object> role) {
                if (role == null) return "";
                return switch (role.get("name").toString()) {
                    case "CLIENT" -> "Клиент";
                    case "MANAGER" -> "Менеджер";
                    case "COURIER" -> "Курьер";
                    case "ADMIN" -> "Админ";
                    default -> role.get("name").toString();
                };
            }

            @Override
            public Map<String, Object> fromString(String string) {
                return null;
            }
        });

        // Устанавливаем текущую роль
        roles.stream()
                .filter(role -> role.get("name").equals(userData.get("roleName")))
                .findFirst()
                .ifPresent(role -> roleChoiceBox.setValue(role));
    }

    @FXML
    private void handleSave() {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();
        Map<String, Object> selectedRole = roleChoiceBox.getValue();

        if (username.isEmpty() || fullName.isEmpty() || selectedRole == null) {
            errorLabel.setText("Заполните имя, полное имя и выберите роль");
            AlertUtil.showError("Заполните имя, полное имя и выберите роль");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("username", username);
                updatedData.put("fullName", fullName);
                updatedData.put("phone", phone);
                updatedData.put("roleId", selectedRole.get("id"));
                if (!password.isEmpty()) {
                    updatedData.put("password", password);
                }
                HttpClientUtil.sendPutRequest("/users/" + userData.get("id"), updatedData);
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            onSaveCallback.run();
            AlertUtil.showInfo("Пользователь успешно обновлен");
            closeWindow();
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка обновления: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}