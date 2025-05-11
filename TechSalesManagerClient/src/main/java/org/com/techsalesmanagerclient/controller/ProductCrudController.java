package org.com.techsalesmanagerclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerclient.client.Client;
import org.com.techsalesmanagerclient.client.JsonUtils;
import org.com.techsalesmanagerclient.client.Request;
import org.com.techsalesmanagerclient.client.Response;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.com.techsalesmanagerclient.enums.ResponseStatus;
import org.com.techsalesmanagerclient.model.Category;
import org.com.techsalesmanagerclient.model.POJO_Product;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ProductCrudController {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextField descriptionField;
    @FXML private TextField stockField;
    @FXML private TableView<POJO_Product> productTable;
    @FXML private TableColumn<POJO_Product, Number> idColumn;
    @FXML private TableColumn<POJO_Product, String> nameColumn;
    @FXML private TableColumn<POJO_Product, String> categoryColumn;
    @FXML private TableColumn<POJO_Product, Number> priceColumn;
    @FXML private TableColumn<POJO_Product, String> descriptionColumn;
    @FXML private TableColumn<POJO_Product, Number> stockColumn;
    @FXML private Button searchButton;
    @FXML private Button createButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button exitButton;

    private final ObservableList<POJO_Product> products = FXCollections.observableArrayList();
    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    private final List<String> expectedKeys = List.of("id", "name", "description", "price", "stock", "category");

    @FXML
    public void initialize() throws IOException, TimeoutException, ClassNotFoundException {
        // Настройка столбцов
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        categoryColumn.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getName() : "");
        });
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        stockColumn.setCellValueFactory(cellData -> cellData.getValue().stockProperty());

        // Заполнение categoryComboBox
        Request categoryRequest = new Request(RequestType.GET_ALL_CATEGORIES, "");
        Response categoryResponse = Client.send(categoryRequest);
        if (categoryResponse.getStatus() == ResponseStatus.Ok) {
            List<Map<String, Object>> rawCategories = JsonUtils.fromJson(categoryResponse.getBody(), List.class);
            List<Category> categories = rawCategories.stream().map(raw -> {
                Category category = new Category();
                category.setId(((Number) raw.get("id")).longValue());
                category.setName((String) raw.get("name"));
                return category;
            }).toList();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        } else {
            log.error("Failed to load categories: {}", categoryResponse.getBody());
            Platform.runLater(() -> showAlert("Error", "Не удалось загрузить категории: " + categoryResponse.getBody()));
        }

        // Добавляем слушатель для выбора записи в TableView
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearInputFields();
            }
        });

        // Загрузка всех товаров
        Request request = new Request(RequestType.GET_ALL_PRODUCTS, "");
        Response response = Client.send(request);
        log.info("Products response: {}", response);
        if (response.getStatus() == ResponseStatus.Ok) {
            List<Map<String, Object>> rawProducts = JsonUtils.fromJson(response.getBody(), List.class);
            for (Map<String, Object> rawProduct : rawProducts) {
                Category category = new Category();
                Map<String, Object> rawCategory = (Map<String, Object>) rawProduct.get("category");
                if (rawCategory != null) {
                    category.setId(((Number) rawCategory.get("id")).longValue());
                    category.setName((String) rawCategory.get("name"));
                }
                products.add(new POJO_Product(
                        ((Number) rawProduct.get("id")).longValue(),
                        (String) rawProduct.get("name"),
                        (String) rawProduct.get("description"),
                        ((Number) rawProduct.get("price")).doubleValue(),
                        ((Number) rawProduct.get("stock")).intValue(),
                        category
                ));
            }
            updateTableView(products);
        } else {
            log.error("Failed to load products: {}", response.getBody());
            Platform.runLater(() -> showAlert("Error", "Не удалось загрузить товары: " + response.getBody()));
        }
    }

    @FXML
    void handleSearch(ActionEvent event) {
        try {
            String idText = idField.getText();
            if (idText == null || idText.trim().isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, введите ID для поиска"));
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idText.trim());
            } catch (NumberFormatException e) {
                log.error("Invalid ID format: {}", idText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат ID: " + idText));
                return;
            }

            Request request = new Request(RequestType.SEARCH_PRODUCT, JsonUtils.toJson(id));
            Response response = Client.send(request);
            log.info("Search response: {}", response);

            if (response.getStatus() == ResponseStatus.Ok) {
                Platform.runLater(() -> {
                    try {
                        products.clear();
                        Map<String, Object> rawProduct = JsonUtils.fromJson(response.getBody(), Map.class);
                        Category category = new Category();
                        Map<String, Object> rawCategory = (Map<String, Object>) rawProduct.get("category");
                        if (rawCategory != null) {
                            category.setId(((Number) rawCategory.get("id")).longValue());
                            category.setName((String) rawCategory.get("name"));
                        }
                        products.add(new POJO_Product(
                                ((Number) rawProduct.get("id")).longValue(),
                                (String) rawProduct.get("name"),
                                (String) rawProduct.get("description"),
                                ((Number) rawProduct.get("price")).doubleValue(),
                                ((Number) rawProduct.get("stock")).intValue(),
                                category
                        ));
                        productTable.setItems(products);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to parse product data", e);
                        showAlert("Error", "Ошибка при обработке данных товара: " + e.getMessage());
                    }
                });
            } else {
                Platform.runLater(() -> showAlert("Error", "Товар не найден: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to search product", e);
            Platform.runLater(() -> showAlert("Error", "Ошибка при поиске: " + e.getMessage()));
        }
    }

    @FXML
    void handleCreate(ActionEvent event) {
        try {
            String name = nameField.getText();
            Category category = categoryComboBox.getValue();
            String priceText = priceField.getText();
            String description = descriptionField.getText();
            String stockText = stockField.getText();

            if (name.isEmpty() || category == null || priceText.isEmpty() || stockText.isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, заполните все обязательные поля"));
                return;
            }

            Double price;
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) throw new NumberFormatException("Цена не может быть отрицательной");
            } catch (NumberFormatException e) {
                log.error("Invalid price format: {}", priceText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат цены: " + e.getMessage()));
                return;
            }

            Integer stock;
            try {
                stock = Integer.parseInt(stockText);
                if (stock < 0) throw new NumberFormatException("Количество не может быть отрицательным");
            } catch (NumberFormatException e) {
                log.error("Invalid stock format: {}", stockText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат количества: " + e.getMessage()));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("description", description);
            data.put("price", price);
            data.put("stock", stock);
            data.put("categoryId", category.getId());

            Request request = new Request(RequestType.CREATE_PRODUCT, JsonUtils.toJson(data));
            Response response = Client.send(request);
            log.info("Create response: {}", response);

            if (response.getStatus() == ResponseStatus.Ok) {
                /*try {
                        handleSearch(event); // Обновляем таблицу
                    } catch (Exception e) {
                        log.error("Failed to refresh table after create", e);
                    }*/
                Platform.runLater(this::clearInputFields);
            } else {
                Platform.runLater(() -> showAlert("Error", "Не удалось создать товар: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to create product", e);
            Platform.runLater(() -> showAlert("Error", "Ошибка при создании товара: " + e.getMessage()));
        }
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/Product_CRUD.fxml",createButton);
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        try {
            if (productTable.getSelectionModel().getSelectedItem() == null) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите товар для обновления"));
                return;
            }

            String idText = idField.getText();
            if (idText == null || idText.isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите товар для обновления"));
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idText);
            } catch (NumberFormatException e) {
                log.error("Invalid ID format: {}", idText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат ID: " + idText));
                return;
            }

            String name = nameField.getText();
            Category category = categoryComboBox.getValue();
            String priceText = priceField.getText();
            String description = descriptionField.getText();
            String stockText = stockField.getText();

            if (name.isEmpty() || category == null || priceText.isEmpty() || stockText.isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, заполните все обязательные поля"));
                return;
            }

            Double price;
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) throw new NumberFormatException("Цена не может быть отрицательной");
            } catch (NumberFormatException e) {
                log.error("Invalid price format: {}", priceText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат цены: " + e.getMessage()));
                return;
            }

            Integer stock;
            try {
                stock = Integer.parseInt(stockText);
                if (stock < 0) throw new NumberFormatException("Количество не может быть отрицательным");
            } catch (NumberFormatException e) {
                log.error("Invalid stock format: {}", stockText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат количества: " + e.getMessage()));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", name);
            data.put("description", description);
            data.put("price", price);
            data.put("stock", stock);
            data.put("categoryId", category.getId());

            Request request = new Request(RequestType.UPDATE_PRODUCT, JsonUtils.toJson(data));
            Response response = Client.send(request);
            log.info("Update response: {}", response);

            if (response.getStatus() == ResponseStatus.Ok) {
                Platform.runLater(() -> {
                    clearInputFields();
                   /* try {
                        handleSearch(event); // Обновляем таблицу
                    } catch (Exception e) {
                        log.error("Failed to refresh table after update", e);
                    }*/
                });
            } else {
                Platform.runLater(() -> showAlert("Error", "Не удалось обновить товар: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to update product", e);
            Platform.runLater(() -> showAlert("Error", "Ошибка при обновлении товара: " + e.getMessage()));
        }
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/Product_CRUD.fxml",createButton);
    }

    @FXML
    void handleDelete(ActionEvent event) {
        try {
            if (productTable.getSelectionModel().getSelectedItem() == null) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите товар для удаления"));
                return;
            }

            String idText = idField.getText();
            if (idText == null || idText.isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите товар для удаления"));
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idText);
            } catch (NumberFormatException e) {
                log.error("Invalid ID format: {}", idText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат ID: " + idText));
                return;
            }

            Request request = new Request(RequestType.DELETE_PRODUCT, JsonUtils.toJson(id));
            Response response = Client.send(request);
            log.info("Delete response: {}", response);

            if (response.getStatus() == ResponseStatus.Ok) {
                Platform.runLater(() -> {
                    products.removeIf(product -> product.getId() != null && product.getId().equals(id));
                    productTable.setItems(FXCollections.observableArrayList(products));
                    clearInputFields();
                    /*try {
                        handleSearch(event); // Обновляем таблицу
                    } catch (Exception e) {
                        log.error("Failed to refresh table after delete", e);
                    }*/
                });
            } else {
                Platform.runLater(() -> showAlert("Error", "Не удалось удалить товар: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to delete product", e);
            Platform.runLater(() -> showAlert("Error", "Ошибка при удалении товара: " + e.getMessage()));
        }
    }

    @FXML
    void handleExit(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Work_Menu.fxml", exitButton);
    }

    private void updateTableView(List<POJO_Product> products) {
        ObservableList<POJO_Product> observableProducts = FXCollections.observableArrayList(products);
        productTable.setItems(observableProducts);
    }

    private void populateFields(POJO_Product product) {
        idField.setText(product.getId() != null ? product.getId().toString() : "");
        nameField.setText(product.getName() != null ? product.getName() : "");
        categoryComboBox.setValue(product.getCategory());
        priceField.setText(product.getPrice() != null ? product.getPrice().toString() : "");
        descriptionField.setText(product.getDescription() != null ? product.getDescription() : "");
        stockField.setText(product.getStock() != null ? product.getStock().toString() : "");
        log.info("Populated fields with product data: {}", product);
    }

    private void clearInputFields() {
        idField.clear();
        nameField.clear();
        categoryComboBox.setValue(null);
        priceField.clear();
        descriptionField.clear();
        stockField.clear();
    }

    private void showAlert(String title, String content) {
        log.error("Showing alert: {} - {}", title, content);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @NotNull
    private Request getRequest() throws JsonProcessingException {
        String name = nameField.getText();
        Category category = categoryComboBox.getValue();
        String priceText = priceField.getText();
        String description = descriptionField.getText();
        String stockText = stockField.getText();

        Double price = priceText.isEmpty() ? null : Double.parseDouble(priceText);
        Integer stock = stockText.isEmpty() ? null : Integer.parseInt(stockText);

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("description", description);
        data.put("price", price);
        data.put("stock", stock);
        if (category != null) {
            data.put("category", Map.of("id", category.getId(), "name", category.getName()));
        }

        return new Request(RequestType.CREATE_PRODUCT, JsonUtils.toJson(data));
    }

    public static String mapToJson(Map<String, Object> data) {
        try {
            String json = JsonUtils.toJson(data);
            log.info("Converted Map to JSON: {}", json);
            return json;
        } catch (Exception e) {
            log.error("Failed to convert Map to JSON: {}", data, e);
            throw new RuntimeException("Failed to convert Map to JSON", e);
        }
    }
}