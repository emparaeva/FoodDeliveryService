package com.example.fooddeliverysystem2.client.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertUtil {

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        // Добавляем CSS
        String cssPath = "com/example/fooddeliverysystem2/client/view/styles.css";
        String cssUrl = AlertUtil.class.getClassLoader().getResource(cssPath) != null
                ? AlertUtil.class.getClassLoader().getResource(cssPath).toExternalForm() : null;
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl);
        }
        alert.showAndWait();
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        // Добавляем CSS
        String cssPath = "com/example/fooddeliverysystem2/client/view/styles.css";
        String cssUrl = AlertUtil.class.getClassLoader().getResource(cssPath) != null
                ? AlertUtil.class.getClassLoader().getResource(cssPath).toExternalForm() : null;
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl);
        }
        alert.showAndWait();
    }
}