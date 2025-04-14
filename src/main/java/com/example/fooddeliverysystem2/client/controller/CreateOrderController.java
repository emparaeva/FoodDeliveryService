package com.example.fooddeliverysystem2.client.controller;

import com.example.fooddeliverysystem2.client.utils.HttpClientUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.TableView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateOrderController {
    @FXML
    private TabPane tabPane;
    @FXML
    private ComboBox<Map<String, Object>> dishComboBox;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField addressField;
    @FXML
    private Label errorLabel;
    @FXML
    private TableView<Map<String, Object>> selectedDishesTable;
    @FXML
    private TableColumn<Map<String, Object>, String> dishNameColumn;
    @FXML
    private TableColumn<Map<String, Object>, Integer> dishQuantityColumn;
    @FXML
    private TableColumn<Map<String, Object>, Void> dishActionColumn;

    private Map<String, Object> userData;
    private ClientMainController parentController;
    private ObservableList<Map<String, Object>> dishes = FXCollections.observableArrayList();
    private ObservableList<Map<String, Object>> selectedDishes = FXCollections.observableArrayList();

    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
    }

    public void setParentController(ClientMainController parentController) {
        this.parentController = parentController;
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
        loadDishes();
        dishComboBox.setItems(dishes);
        dishComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> dish, boolean empty) {
                super.updateItem(dish, empty);
                setText(empty || dish == null ? "" : dish.get("name").toString());
            }
        });
        dishComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> dish, boolean empty) {
                super.updateItem(dish, empty);
                setText(empty || dish == null ? "" : dish.get("name").toString());
            }
        });

        // Настройка таблицы выбранных блюд
        dishNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().get("name").toString()));
        dishQuantityColumn.setCellValueFactory(cell -> new SimpleIntegerProperty((Integer) cell.getValue().get("quantity")).asObject());
        dishActionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Удалить");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                deleteButton.setOnAction(event -> {
                    Map<String, Object> dish = getTableView().getItems().get(getIndex());
                    selectedDishes.remove(dish);
                });
                setGraphic(deleteButton);
            }
        });
        selectedDishesTable.setItems(selectedDishes);
    }

    private void loadDishes() {
        try {
            String response = HttpClientUtil.sendGetRequest("/dishes/available");
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> dishList = mapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
            dishes.setAll(dishList);
        } catch (IOException e) {
            errorLabel.setText("Ошибка загрузки блюд: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddDish() {
        Map<String, Object> selectedDish = dishComboBox.getSelectionModel().getSelectedItem();
        String quantityText = quantityField.getText();

        if (selectedDish == null) {
            errorLabel.setText("Выберите блюдо");
            return;
        }
        if (quantityText.isEmpty() || !quantityText.matches("\\d+")) {
            errorLabel.setText("Введите корректное количество");
            return;
        }

        int quantity = Integer.parseInt(quantityText);
        Map<String, Object> dishEntry = new HashMap<>();
        dishEntry.put("dishId", selectedDish.get("id"));
        dishEntry.put("name", selectedDish.get("name"));
        dishEntry.put("quantity", quantity);
        selectedDishes.add(dishEntry);

        // Очищаем поля для следующего ввода
        dishComboBox.getSelectionModel().clearSelection();
        quantityField.clear();
        errorLabel.setText("");
    }

    @FXML
    private void handleCreateOrder() {
        try {
            if (selectedDishes.isEmpty()) {
                errorLabel.setText("Добавьте хотя бы одно блюдо");
                return;
            }
            String address = addressField.getText();
            if (address.isEmpty()) {
                errorLabel.setText("Введите адрес");
                return;
            }

            System.out.println("Client sending address: " + address);

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("userId", userData.get("id"));
            orderData.put("deliveryAddress", address);
            orderData.put("orderDate", java.time.LocalDateTime.now().toString());
            List<Map<String, Object>> dishList = new ArrayList<>();
            for (Map<String, Object> dishEntry : selectedDishes) {
                Map<String, Object> dish = new HashMap<>();
                dish.put("dishId", dishEntry.get("dishId"));
                dish.put("quantity", dishEntry.get("quantity"));
                dishList.add(dish);
            }
            orderData.put("dishes", dishList);

            String response = HttpClientUtil.sendPostRequest("/orders", orderData);
            System.out.println("Create order response: " + response);

            parentController.refreshOrders();
            Stage stage = (Stage) addressField.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            errorLabel.setText("Ошибка создания заказа: " + e.getMessage());
            System.out.println("Error creating order: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) addressField.getScene().getWindow();
        stage.close();
    }
}