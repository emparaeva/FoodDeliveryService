package com.example.fooddeliverysystem2.client.controller;

import com.example.fooddeliverysystem2.client.utils.AlertUtil;
import com.example.fooddeliverysystem2.client.utils.HttpClientUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourierMainController {

    @FXML
    private TabPane tabPane;
    @FXML
    private TableView<Map<String, Object>> ordersTable;
    @FXML
    private TableColumn<Map<String, Object>, String> orderIdColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> statusOrderColumn;
    @FXML
    private TableColumn<Map<String, Object>, Double> totalPriceColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> deliveryAddressColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> createdAtColumn;
    @FXML
    private TableColumn<Map<String, Object>, Void> actionColumn;
    @FXML
    private Label errorLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutButton;

    private Map<String, Object> userData;
    private ObservableList<Map<String, Object>> orders = FXCollections.observableArrayList();

    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
        usernameLabel.setText((String) userData.get("fullName"));
        String phone = (String) userData.get("phone");
        phoneLabel.setText(phone != null ? phone : "Не указан");
        loadOrdersAsync();
    }

    @FXML
    private void initialize() {
        try {
            String cssPath = "com/example/fooddeliverysystem2/client/view/styles.css";
            if (getClass().getClassLoader().getResourceAsStream(cssPath) == null) {
                System.out.println("ControllerName: CSS не найден: " + cssPath);
            } else {
                System.out.println("ControllerName: CSS найден: " + cssPath);
            }
            if (tabPane!= null) { // Замени someElement на любой @FXML-элемент, например, tabPane
                tabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                    if (newScene != null) {
                        try {
                            String cssUrl = getClass().getClassLoader().getResource(cssPath) != null
                                    ? getClass().getClassLoader().getResource(cssPath).toExternalForm() : null;
                            if (cssUrl != null) {
                                newScene.getStylesheets().add(cssUrl);
                                System.out.println("ControllerName: CSS успешно добавлен: " + cssUrl);
                            } else {
                                System.out.println("ControllerName: CSS не добавлен: " + cssPath);
                            }
                        } catch (Exception e) {
                            System.out.println("ControllerName: Исключение при добавлении CSS: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("ControllerName: Сцена не готова");
                    }
                });
            } else {
                System.out.println("ControllerName: someElement is null");
            }
        } catch (Exception e) {
            System.out.println("ControllerName: Ошибка инициализации CSS: " + e.getMessage());
        }
        orderIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("id").toString()));
        statusOrderColumn.setCellValueFactory(cell -> {
            String status = cell.getValue().get("status").toString();
            String displayStatus = switch (status) {
                case "ASSIGNED" -> "Назначен";
                case "DELIVERED" -> "Доставлен";
                case "CANCELED" -> "Отменён"; // Добавили перевод для CANCELED
                default -> status;
            };
            return new SimpleStringProperty(displayStatus);
        });
        totalPriceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().get("totalPrice") != null ? Double.valueOf(cell.getValue().get("totalPrice").toString()) : 0.0));
        deliveryAddressColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("deliveryAddress") != null ? cell.getValue().get("deliveryAddress").toString() : ""));
        createdAtColumn.setCellValueFactory(cell -> {
            String createdAt = cell.getValue().get("createdAt") != null ? cell.getValue().get("createdAt").toString() : "";
            return new SimpleStringProperty(createdAt);
        });
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deliverButton = new Button("Доставлен");
            private final Button cancelButton = new Button("Отменить"); // Новая кнопка

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Map<String, Object> order = getTableView().getItems().get(getIndex());
                String status = order.get("status").toString();
                if ("ASSIGNED".equals(status)) {
                    Long orderId = Long.valueOf(order.get("id").toString());
                    deliverButton.setOnAction(event -> handleMarkDelivered(orderId));
                    cancelButton.setOnAction(event -> handleCancelOrder(orderId)); // Обработчик для отмены
                    HBox hBox = new HBox(5, deliverButton, cancelButton); // Две кнопки рядом
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });
        ordersTable.setItems(orders);

        logoutButton.setOnAction(event -> handleLogout());
    }

    private void handleMarkDelivered(Long orderId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClientUtil.sendPostRequest("/orders/" + orderId + "/delivered", new HashMap<>());
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            loadOrdersAsync();
            errorLabel.setText("Заказ доставлен");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void handleCancelOrder(Long orderId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClientUtil.sendPostRequest("/orders/" + orderId + "/setCanceled", new HashMap<>());
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            loadOrdersAsync();
            errorLabel.setText("Заказ отменён");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка отмены заказа: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка отмены заказа: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void loadOrdersAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String userId = userData.get("id").toString();
                String response = HttpClientUtil.sendGetRequest("/orders/courier/" + userId);
                System.out.println("Orders response: " + response);
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> orderList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                Platform.runLater(() -> orders.setAll(orderList));
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            errorLabel.setText(orders.isEmpty() ? "Нет назначенных заказов" : "");
            System.out.println("Loaded orders: " + orders.size());
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            orders.clear();
            errorLabel.setText("Ошибка загрузки заказов: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка загрузки заказов: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/fooddeliverysystem2/client/view/login.fxml"));
            stage.setScene(new Scene(root, 400, 300));
            stage.setTitle("Food Delivery System - Login");
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Ошибка выхода: " + e.getMessage());
            AlertUtil.showError("Ошибка выхода: " + e.getMessage());
        }
    }
}



