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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerMainController {

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
    private TableColumn<Map<String, Object>, String> courierColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> createdAtColumn;
    @FXML
    private TableColumn<Map<String, Object>, Void> actionColumn;
    @FXML
    private TableView<Map<String, Object>> dishesTable;
    @FXML
    private TableColumn<Map<String, Object>, String> nameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> descriptionColumn; // Новый столбец
    @FXML
    private TableColumn<Map<String, Object>, Double> priceColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> availableColumn;
    @FXML
    private TableColumn<Map<String, Object>, Void> dishActionColumn;
    @FXML
    private TextField newDishName;
    @FXML
    private TextField newDishDescription; // Новое поле
    @FXML
    private TextField newDishPrice;
    @FXML
    private Button addDishButton;
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
    private ObservableList<Map<String, Object>> dishes = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> couriers = FXCollections.observableArrayList();

    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
        usernameLabel.setText((String) userData.get("fullName"));
        String phone = (String) userData.get("phone");
        phoneLabel.setText(phone != null ? phone : "Не указан");
        loadOrdersAsync();
        loadDishes();
        loadCouriers();
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
            if (tabPane != null) { // Замени someElement на любой @FXML-элемент, например, tabPane
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
        // Таблица заказов
        orderIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("id").toString()));
        statusOrderColumn.setCellValueFactory(cell -> {
            String status = cell.getValue().get("status").toString();
            String displayStatus = switch (status) {
                case "NEW" -> "Новый";
                case "ASSIGNED" -> "Назначен";
                case "DELIVERED" -> "Доставлен";
                case "CANCELED" -> "Отменён"; // Исправили
                default -> status;
            };
            return new SimpleStringProperty(displayStatus);
        });
        totalPriceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().get("totalPrice") != null ? Double.valueOf(cell.getValue().get("totalPrice").toString()) : 0.0));
        deliveryAddressColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("deliveryAddress") != null ? cell.getValue().get("deliveryAddress").toString() : ""));
        courierColumn.setCellValueFactory(cell -> {
            String courierName = cell.getValue().get("courierName") != null ? cell.getValue().get("courierName").toString() : "Не назначен";
            return new SimpleStringProperty(courierName);
        });
        createdAtColumn.setCellValueFactory(cell -> {
            String createdAt = cell.getValue().get("createdAt") != null ? cell.getValue().get("createdAt").toString() : "";
            return new SimpleStringProperty(createdAt);
        });
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Map<String, Object>> courierCombo = new ComboBox<>();
            private final Button assignButton = new Button("Назначить");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Map<String, Object> order = getTableView().getItems().get(getIndex());
                String status = order.get("status").toString();
                if ("NEW".equals(status)) {
                    courierCombo.setItems(couriers);
                    courierCombo.setCellFactory(lv -> new ListCell<>() {
                        @Override
                        protected void updateItem(Map<String, Object> courier, boolean empty) {
                            super.updateItem(courier, empty);
                            setText(empty || courier == null ? "" : courier.get("fullName").toString());
                        }
                    });
                    courierCombo.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(Map<String, Object> courier, boolean empty) {
                            super.updateItem(courier, empty);
                            setText(empty || courier == null ? "Выберите курьера" : courier.get("fullName").toString());
                        }
                    });
                    Long orderId = Long.valueOf(order.get("id").toString());
                    assignButton.setOnAction(event -> {
                        Map<String, Object> selectedCourier = courierCombo.getSelectionModel().getSelectedItem();
                        if (selectedCourier != null) {
                            handleAssignCourier(orderId, Long.valueOf(selectedCourier.get("id").toString()));
                        } else {
                            AlertUtil.showError("Выберите курьера");
                        }
                    });
                    HBox hBox = new HBox(5, courierCombo, assignButton);
                    setGraphic(hBox);
                } else {
                    setGraphic(null);
                }
            }
        });
        ordersTable.setItems(orders);

        // Таблица блюд
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("name").toString()));
        descriptionColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("description") != null ? cell.getValue().get("description").toString() : ""));
        priceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(Double.valueOf(cell.getValue().get("price").toString())));
        availableColumn.setCellValueFactory(cell -> new SimpleStringProperty(Boolean.valueOf(cell.getValue().get("available").toString()) ? "Да" : "Нет"));
        dishActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Удалить");
            private final Button editButton = new Button("Редактировать");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Map<String, Object> dish = getTableView().getItems().get(getIndex());
                Long dishId = Long.valueOf(dish.get("id").toString());
                deleteButton.setOnAction(event -> handleDeleteDish(dishId));
                editButton.setOnAction(event -> handleEditDish(dish));
                HBox hBox = new HBox(5, deleteButton, editButton);
                setGraphic(hBox);
            }
        });
        dishesTable.setItems(dishes);

        logoutButton.setOnAction(event -> handleLogout());
    }

    private void handleAssignCourier(Long orderId, Long courierId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Map<String, Object> data = new HashMap<>();
                data.put("courierId", courierId);
                HttpClientUtil.sendPostRequest("/orders/" + orderId + "/assign", data);
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            loadOrdersAsync();
            errorLabel.setText("Курьер назначен");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка назначения курьера: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка назначения курьера: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void handleDeleteDish(Long dishId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClientUtil.sendDeleteRequest("/dishes/" + dishId); // Исправили URL
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            loadDishes();
            errorLabel.setText("Блюдо удалено");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка удаления блюда: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка удаления блюда: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void handleEditDish(Map<String, Object> dish) {
        TextField nameField = new TextField(dish.get("name").toString());
        TextField descriptionField = new TextField(dish.get("description") != null ? dish.get("description").toString() : "");
        TextField priceField = new TextField(dish.get("price").toString());
        CheckBox availableBox = new CheckBox("Доступно");
        availableBox.setSelected(Boolean.parseBoolean(dish.get("available").toString()));
        Button saveButton = new Button("Сохранить");

        VBox dialogPane = new VBox(10,
                new Label("Название:"), nameField,
                new Label("Описание:"), descriptionField,
                new Label("Цена:"), priceField,
                availableBox, saveButton);
        dialogPane.setPadding(new Insets(10));
        Stage dialog = new Stage();
        dialog.setScene(new Scene(dialogPane, 300, 250));
        dialog.setTitle("Редактировать блюдо");
        dialog.show();

        saveButton.setOnAction(event -> {
            try {
                String name = nameField.getText();
                String description = descriptionField.getText();
                double price = Double.parseDouble(priceField.getText());
                boolean available = availableBox.isSelected();
                if (name.isEmpty()) {
                    AlertUtil.showError("Название не оно быть пустым");
                    return;
                }
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Map<String, Object> dishData = new HashMap<>();
                        dishData.put("name", name);
                        dishData.put("description", description);
                        dishData.put("price", price);
                        dishData.put("available", available);
                        HttpClientUtil.sendPutRequest("/dishes/" + dish.get("id"), dishData); // Исправили URL
                        return null;
                    }
                };

                task.setOnSucceeded(e -> Platform.runLater(() -> {
                    loadDishes();
                    errorLabel.setText("Блюдо обновлено");
                    dialog.close();
                }));

                task.setOnFailed(e -> Platform.runLater(() -> {
                    errorLabel.setText("Ошибка обновления блюда: " + task.getException().getMessage());
                    AlertUtil.showError("Ошибка обновления блюда: " + task.getException().getMessage());
                }));

                new Thread(task).start();
            } catch (NumberFormatException e) {
                AlertUtil.showError("Цена должна быть числом");
            }
        });
    }

    @FXML
    private void handleAddDish() {
        String name = newDishName.getText();
        String description = newDishDescription.getText();
        String priceStr = newDishPrice.getText();

        if (name.isEmpty() || priceStr.isEmpty()) {
            errorLabel.setText("Введите название и цену");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Map<String, Object> dishData = new HashMap<>();
                    dishData.put("name", name);
                    dishData.put("description", description);
                    dishData.put("price", price);
                    dishData.put("available", true);
                    HttpClientUtil.sendPostRequest("/dishes", dishData); // Исправили URL
                    return null;
                }
            };

            task.setOnSucceeded(event -> Platform.runLater(() -> {
                loadDishes();
                newDishName.clear();
                newDishDescription.clear();
                newDishPrice.clear();
                errorLabel.setText("Блюдо добавлено");
            }));

            task.setOnFailed(event -> Platform.runLater(() -> {
                errorLabel.setText("Ошибка добавления блюда: " + task.getException().getMessage());
                AlertUtil.showError("Ошибка добавления блюда: " + task.getException().getMessage());
            }));

            new Thread(task).start();
        } catch (NumberFormatException e) {
            errorLabel.setText("Цена должна быть числом");
            AlertUtil.showError("Цена должна быть числом");
        }
    }

    private void loadOrdersAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String response = HttpClientUtil.sendGetRequest("/orders/all");
                System.out.println("Orders response: " + response);
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> orderList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                Platform.runLater(() -> orders.setAll(orderList));
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            errorLabel.setText(orders.isEmpty() ? "Нет заказов" : "");
            System.out.println("Loaded orders: " + orders.size());
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            orders.clear();
            errorLabel.setText("Ошибка загрузки заказов: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка загрузки заказов: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void loadDishes() {
        try {
            String response = HttpClientUtil.sendGetRequest("/dishes");
            System.out.println("Dishes response: " + response);
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> dishList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
            Platform.runLater(() -> {
                dishes.setAll(dishList);
                errorLabel.setText(dishList.isEmpty() ? "Нет блюд" : "");
            });
        } catch (IOException e) {
            Platform.runLater(() -> {
                errorLabel.setText("Ошибка загрузки блюд: " + e.getMessage());
                AlertUtil.showError("Ошибка загрузки блюд: " + e.getMessage());
            });
        }
    }

    private void loadCouriers() {
        try {
            String response = HttpClientUtil.sendGetRequest("/users/couriers");
            System.out.println("Couriers response: " + response);
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> courierList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
            Platform.runLater(() -> couriers.setAll(courierList));
        } catch (IOException e) {
            Platform.runLater(() -> {
                errorLabel.setText("Ошибка загрузки курьеров: " + e.getMessage());
                AlertUtil.showError("Ошибка загрузки курьеров: " + e.getMessage());
            });
        }
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