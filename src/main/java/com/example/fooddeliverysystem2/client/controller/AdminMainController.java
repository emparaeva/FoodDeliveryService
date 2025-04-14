package com.example.fooddeliverysystem2.client.controller;

import com.example.fooddeliverysystem2.client.utils.AlertUtil;
import com.example.fooddeliverysystem2.client.utils.HttpClientUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javafx.scene.layout.HBox;

public class AdminMainController {

    @FXML
    private TabPane tabPane;
    @FXML
    private TableView<Map<String, Object>> usersTable;
    @FXML
    private TableColumn<Map<String, Object>, String> userIdColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> usernameColumn;
    @FXML
    private TableColumn<Map<String, Object>, String> roleColumn;
    @FXML
    private TableColumn<Map<String, Object>, Void> userActionColumn;
    @FXML
    private TextField newUsername;
    @FXML
    private TextField newPassword;
    @FXML
    private ChoiceBox<Map<String, Object>> newUserRole;
    @FXML
    private Button addUserButton;
    @FXML
    private Label errorLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private TextField searchField;
    @FXML
    private Button clearSearchButton;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalOrdersLabel;
    @FXML
    private Label avgDeliveryTimeLabel;
    @FXML
    private BarChart<String, Number> ordersChart;

    private Map<String, Object> userData;
    private ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> roles = FXCollections.observableArrayList();
    private FilteredList<Map<String, Object>> filteredUsers;

    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
        usernameLabel.setText((String) userData.get("fullName"));
        String phone = (String) userData.get("phone");
        phoneLabel.setText(phone != null ? phone : "Не указан");
        loadRolesAsync();
        loadUsersAsync();
        loadStatsAsync();
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
        userIdColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("id").toString()));
        usernameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("fullName").toString()));
        roleColumn.setCellValueFactory(cell -> {
            String roleName = cell.getValue().get("roleName") != null ? cell.getValue().get("roleName").toString() : "";
            String displayRole = switch (roleName) {
                case "CLIENT" -> "Клиент";
                case "MANAGER" -> "Менеджер";
                case "COURIER" -> "Курьер";
                case "ADMIN" -> "Админ";
                default -> roleName;
            };
            return new SimpleStringProperty(displayRole);
        });
        userActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Редактировать");
            private final Button deleteButton = new Button("Удалить");

            {
                editButton.setStyle("-fx-margin-right: 5px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Map<String, Object> user = getTableView().getItems().get(getIndex());
                Long userId = Long.valueOf(user.get("id").toString());
                if (userId.equals(userData.get("id"))) {
                    setGraphic(null);
                    return;
                }
                editButton.setOnAction(event -> handleEditUser(user));
                deleteButton.setOnAction(event -> handleDeleteUser(userId));
                HBox hbox = new HBox(5, editButton, deleteButton);
                setGraphic(hbox);
            }
        });

        // Настройка сортировки
        usernameColumn.setComparator(String::compareToIgnoreCase);
        roleColumn.setComparator(String::compareToIgnoreCase);

        // Настройка фильтрации
        filteredUsers = new FilteredList<>(users, p -> true);
        usersTable.setItems(filteredUsers);

        // Обработчик изменения текста в поисковом поле
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(createPredicate(newValue));
        });

        newUserRole.setConverter(new StringConverter<Map<String, Object>>() {
            @Override
            public String toString(Map<String, Object> role) {
                if (role == null) {
                    return "Выберите роль";
                }
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
        newUserRole.setItems(roles);

        logoutButton.setOnAction(event -> handleLogout());
        addUserButton.setOnAction(event -> handleAddUser());
        clearSearchButton.setOnAction(event -> handleClearSearch());
    }

    private Predicate<Map<String, Object>> createPredicate(String searchText) {
        return user -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return user.get("fullName").toString().toLowerCase().contains(lowerCaseFilter) ||
                    user.get("username").toString().toLowerCase().contains(lowerCaseFilter);
        };
    }

    @FXML
    private void handleSearch() {
        // Поиск активируется автоматически
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        filteredUsers.setPredicate(p -> true);
    }

    private void handleEditUser(Map<String, Object> user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fooddeliverysystem2/client/view/edit_user.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            controller.setUserData(user, roles, this::loadUsersAsync);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Редактировать пользователя");
            stage.setScene(new Scene(root, 500, 400)); // Увеличили размеры окна
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            errorLabel.setText("Ошибка открытия окна: " + e.getMessage());
            AlertUtil.showError("Ошибка открытия окна: " + e.getMessage());
        }
    }

    private void handleDeleteUser(Long userId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClientUtil.sendDeleteRequest("/users/" + userId);
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            loadUsersAsync();
            errorLabel.setText("Пользователь удалён");
            AlertUtil.showInfo("Пользователь успешно удалён");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка удаления: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка удаления: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    @FXML
    private void handleAddUser() {
        String username = newUsername.getText().trim();
        String password = newPassword.getText().trim();
        Map<String, Object> selectedRole = newUserRole.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedRole == null) {
            errorLabel.setText("Введите имя, пароль и выберите роль");
            AlertUtil.showError("Введите имя, пароль и выберите роль");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", username);
                userData.put("password", password);
                userData.put("fullName", username);
                userData.put("roleId", selectedRole.get("id"));
                userData.put("phone", "");
                userData.put("address", "");
                HttpClientUtil.sendPostRequest("/users", userData);
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            loadUsersAsync();
            newUsername.clear();
            newPassword.clear();
            newUserRole.getSelectionModel().clearSelection();
            errorLabel.setText("Пользователь добавлен");
            AlertUtil.showInfo("Пользователь успешно добавлен");
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка добавления: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка добавления: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void loadUsersAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String response = HttpClientUtil.sendGetRequest("/users");
                System.out.println("Users response: " + response);
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> userList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                Platform.runLater(() -> {
                    users.setAll(userList);
                    totalUsersLabel.setText(String.valueOf(userList.size()));
                });
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            errorLabel.setText(users.isEmpty() ? "Нет пользователей" : "");
            System.out.println("Loaded users: " + users.size());
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            users.clear();
            totalUsersLabel.setText("0");
            errorLabel.setText("Ошибка загрузки пользователей: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка загрузки пользователей: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void loadRolesAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String response = HttpClientUtil.sendGetRequest("/roles");
                System.out.println("Roles response: " + response);
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> roleList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
                Platform.runLater(() -> roles.setAll(roleList));
                return null;
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            if (!roles.isEmpty() && newUserRole.getValue() == null) {
                newUserRole.setValue(roles.get(0));
            }
        }));

        task.setOnFailed(event -> Platform.runLater(() -> {
            errorLabel.setText("Ошибка загрузки ролей: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка загрузки ролей: " + task.getException().getMessage());
        }));

        new Thread(task).start();
    }

    private void loadStatsAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String response = HttpClientUtil.sendGetRequest("/orders/stats");
                System.out.println("Stats response: " + response);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> stats = mapper.readValue(response, new TypeReference<Map<String, Object>>() {});
                Platform.runLater(() -> {
                    totalOrdersLabel.setText(String.valueOf(stats.get("totalOrders")));
                    double avgTime = ((Number) stats.get("avgDeliveryTime")).doubleValue();
                    avgDeliveryTimeLabel.setText(String.format("%.1f мин", avgTime));

                    @SuppressWarnings("unchecked")
                    Map<String, Number> statusCounts = (Map<String, Number>) stats.get("statusCounts");
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Заказы");
                    statusCounts.forEach((status, count) -> {
                        String displayStatus = switch (status) {
                            case "NEW" -> "Новый";
                            case "ASSIGNED" -> "Назначен";
                            case "DELIVERED" -> "Доставлен";
                            case "CANCELED" -> "Отменен";
                            default -> status;
                        };
                        series.getData().add(new XYChart.Data<>(displayStatus, count));
                    });
                    ordersChart.getData().clear();
                    ordersChart.getData().add(series);
                });
                return null;
            }
        };

        task.setOnFailed(event -> Platform.runLater(() -> {
            totalOrdersLabel.setText("0");
            avgDeliveryTimeLabel.setText("0 мин");
            ordersChart.getData().clear();
            errorLabel.setText("Ошибка загрузки статистики: " + task.getException().getMessage());
            AlertUtil.showError("Ошибка загрузки статистики: " + task.getException().getMessage());
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


