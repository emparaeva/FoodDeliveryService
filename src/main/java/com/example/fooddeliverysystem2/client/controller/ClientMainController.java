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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientMainController {

    @FXML
    private TabPane tabPane;
    @FXML
    private TableView<Map<String, Object>> dishesTable;
    @FXML
    private TableColumn<Map<String, Object>, String> nameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> descriptionColumn;
    @FXML
    private TableColumn<Map<String, Object>, Double> priceColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> categoryColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> availableColumn;
    @FXML
    private TableView<Map<String, Object>> ordersTable;
    @FXML
    private TableColumn<Map<String, Object>, String> orderIdColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> dishCompositionColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> statusOrderColumn;
    @FXML
    private TableColumn<Map<String, Object>, Double> totalPriceColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> deliveryAddressColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> createdAtColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> deliveredAtColumn;
    @FXML
    private TableColumn<Map<String, Object>, Void> actionColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button createOrderButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Label aboutTitleLabel;
    @FXML
    private Label aboutDeveloperLabel;
    @FXML
    private Label aboutDescriptionLabel;
    @FXML
    private Label aboutContactLabel;

    private Map<String, Object> userData;
    private ObservableList<Map<String, Object>> dishes = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> allDishes = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> orders = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
        usernameLabel.setText((String) userData.get("fullName"));
        String phone = (String) userData.get("phone");
        phoneLabel.setText(phone != null ? phone : "Не указан");
        loadDishes();
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

        // Инициализация вкладки "Об авторе"
        aboutTitleLabel.setText("Food Delivery System");
        aboutDeveloperLabel.setText("Разработчик: Параева Елена");
        aboutDescriptionLabel.setText("Приложение для заказа еды с доставкой.");
        aboutContactLabel.setText("Контакты: 222592@edu.fa.ru");

        // Таблица блюд
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("name").toString()));
        descriptionColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("description") != null ? cell.getValue().get("description").toString() : ""));
        priceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(Double.valueOf(cell.getValue().get("price").toString())));
        categoryColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("category") != null ? cell.getValue().get("category").toString() : ""));
        availableColumn.setCellValueFactory(cell -> new SimpleStringProperty(Boolean.valueOf(cell.getValue().get("available").toString()) ? "Да" : "Нет"));
        dishesTable.setItems(dishes);

        // Таблица заказов
        orderIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("id").toString()));
        dishCompositionColumn.setCellValueFactory(cell -> {
            List<Map<String, Object>> dishes = (List<Map<String, Object>>) cell.getValue().get("dishes");
            if (dishes == null || dishes.isEmpty()) {
                return new SimpleStringProperty("Пусто");
            }
            String composition = dishes.stream()
                    .map(dish -> dish.get("name") + " — " + dish.get("quantity") + " шт.")
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(composition);
        });
        statusOrderColumn.setCellValueFactory(cell -> {
            String status = cell.getValue().get("status").toString();
            String displayStatus = switch (status) {
                case "NEW" -> "Новый";
                case "ASSIGNED" -> "Назначен";
                case "DELIVERED" -> "Доставлен";
                case "CANCELED" -> "Отменён";
                default -> status;
            };
            return new SimpleStringProperty(displayStatus);
        });
        totalPriceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().get("totalPrice") != null ? Double.valueOf(cell.getValue().get("totalPrice").toString()) : 0.0));
        deliveryAddressColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("deliveryAddress") != null ? cell.getValue().get("deliveryAddress").toString() : ""));
        createdAtColumn.setCellValueFactory(cell -> {
            String createdAt = (String) cell.getValue().get("createdAt");
            if (createdAt == null) return new SimpleStringProperty("-");
            LocalDateTime dateTime = LocalDateTime.parse(createdAt);
            return new SimpleStringProperty(dateTime.format(DATE_FORMATTER));
        });
        deliveredAtColumn.setCellValueFactory(cell -> {
            String deliveredAt = (String) cell.getValue().get("deliveredAt");
            if (deliveredAt == null) return new SimpleStringProperty("-");
            LocalDateTime dateTime = LocalDateTime.parse(deliveredAt);
            return new SimpleStringProperty(dateTime.format(DATE_FORMATTER));
        });
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button cancelButton = new Button("Отменить");
            private final Button deleteButton = new Button("Удалить");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Map<String, Object> order = getTableView().getItems().get(getIndex());
                String status = order.get("status").toString();
                Long orderId = Long.valueOf(order.get("id").toString());
                cancelButton.getStyleClass().add("action-button");
                deleteButton.getStyleClass().add("action-button");
                if ("NEW".equals(status)) {
                    cancelButton.setOnAction(event -> handleCancelOrder(orderId));
                    setGraphic(cancelButton);
                } else if ("CANCELED".equals(status)) {
                    deleteButton.setOnAction(event -> handleDeleteOrder(orderId));
                    setGraphic(deleteButton);
                } else {
                    setGraphic(null);
                }
            }
        });
        ordersTable.setItems(orders);

        logoutButton.setOnAction(event -> handleLogout());
        createOrderButton.setOnAction(event -> handleCreateOrder());
    }

    private void handleCancelOrder(Long orderId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("Sending cancel request for order: " + orderId);
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

    private void handleDeleteOrder(Long orderId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                System.out.println("Sending delete request for order: " + orderId);
                HttpClientUtil.sendDeleteRequest("/orders/" + orderId);
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            loadOrdersAsync();
            errorLabel.setText("Заказ удалён");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка удаления заказа: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка удаления заказа: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void loadDishes() {
        try {
            String response = HttpClientUtil.sendGetRequest("/dishes/available");
            System.out.println("Dishes response: " + response);
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> dishList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
            Platform.runLater(() -> {
                allDishes.setAll(dishList);
                dishes.setAll(dishList);
                errorLabel.setText(dishList.isEmpty() ? "Нет доступных блюд" : "");
            });
        } catch (IOException e) {
            Platform.runLater(() -> {
                errorLabel.setText("Ошибка загрузки блюд: " + e.getMessage());
                AlertUtil.showError("Ошибка загрузки блюд: " + e.getMessage());
            });
        }
    }

    private void loadOrdersAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String userId = userData.get("id").toString();
                String response = HttpClientUtil.sendGetRequest("/orders/user/" + userId);
                System.out.println("Orders response: " + response);
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> orderList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                Platform.runLater(() -> orders.setAll(orderList));
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            errorLabel.setText(orders.isEmpty() ? "У вас нет заказов" : "");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            orders.clear();
            errorLabel.setText("Ошибка загрузки заказов: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка загрузки заказов: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleCreateOrder() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fooddeliverysystem2/client/view/create_order.fxml"));
            Parent root = loader.load();
            CreateOrderController controller = loader.getController();
            controller.setUserData(userData);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Создать заказ");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            errorLabel.setText("Ошибка открытия формы заказа: " + e.getMessage());
            AlertUtil.showError("Ошибка открытия формы заказа: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            dishes.setAll(allDishes);
        } else {
            List<Map<String, Object>> filtered = allDishes.stream()
                    .filter(dish -> dish.get("name").toString().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            dishes.setAll(filtered);
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

    public void refreshOrders() {
        loadOrdersAsync();
        tabPane.getSelectionModel().select(2);
    }
}