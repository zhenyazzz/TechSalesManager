package org.com.techsalesmanagerclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import org.com.techsalesmanagerclient.service.ProductFilterService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
public class ProductWorkController {

    @FXML private ComboBox<String> filterComboBox;
    @FXML private TableColumn<POJO_Product, Number> idColumn;
    @FXML private TableColumn<POJO_Product, String> nameColumn;
    @FXML private TableColumn<POJO_Product, String> categoryColumn;
    @FXML private TableColumn<POJO_Product, Number> priceColumn;
    @FXML private TableColumn<POJO_Product, String> descriptionColumn;
    @FXML private TableColumn<POJO_Product, Number> stockColumn;
    @FXML private Button searchButton;
    @FXML private Button submitButton;
    @FXML private TextField filterField;
    @FXML private Button exitButton;
    @FXML private TextField searchField;
    @FXML private TableView<POJO_Product> productTable;

    private final ObservableList<POJO_Product> products = FXCollections.observableArrayList();
    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    private final List<String> expectedKeys = List.of("id", "name", "description", "price", "stock", "category");

    public ProductWorkController() throws InterruptedException {
    }

    @FXML
    public void initialize() throws IOException, TimeoutException, ClassNotFoundException {
        filterComboBox.getItems().add("По ID");
        filterComboBox.getItems().add("По названию");

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
    void handleExit(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/Product_Work_Menu.fxml", exitButton);
    }

    @FXML
    void handleFilterAndSort(ActionEvent event) {
        try {
            String filterType = filterComboBox.getValue();
            String filterValue = filterField.getText();
            Response response = ProductFilterService.filter(filterType, filterValue);
            if (response.getStatus() == ResponseStatus.Ok) {
                clearAndFillTableWithResponse(response);
            } else {
                Platform.runLater(() -> showAlert("Error", "Не удалось выполнить фильтрацию: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to perform filter", e);
            Platform.runLater(() -> showAlert("Error", "Ошибка при фильтрации: " + e.getMessage()));
        }
    }

    @FXML
    void handleSearch(ActionEvent event) {
        try {
            String idText = searchField.getText();
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
                clearAndFillTableWithSingleProduct(response);
            } else {
                log.error("Search failed: {}", response.getBody());
                Platform.runLater(() -> showAlert("Error", "Товар не найден: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to search product", e);
            Platform.runLater(() -> showAlert("Error", "Ошибка при поиске: " + e.getMessage()));
        }
    }

    private void clearAndFillTableWithSingleProduct(Response response) {
        log.info("Clearing and filling TableView with single product: {}", response.getBody());
        Platform.runLater(() -> {
            products.clear();
            try {
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
                log.debug("TableView updated with single product: {}", products);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse product from response: {}", response.getBody(), e);
                throw new RuntimeException("Failed to parse product", e);
            }
        });
    }

    private void updateTableView(List<POJO_Product> products) {
        ObservableList<POJO_Product> observableProducts = FXCollections.observableArrayList(products);
        productTable.setItems(observableProducts);
    }

    private Map<String, Object> normalizeMap(Map<String, Object> input) {
        log.info("Normalizing map: {}", input);
        Map<String, Object> normalized = new HashMap<>();
        for (String key : expectedKeys) {
            normalized.put(key, input.getOrDefault(key, null));
        }
        if (!input.keySet().stream().allMatch(expectedKeys::contains)) {
            List<String> extraKeys = input.keySet().stream()
                    .filter(key -> !expectedKeys.contains(key))
                    .collect(Collectors.toList());
            log.warn("Found extra keys in response: {}", extraKeys);
        }
        return normalized;
    }

    private void showAlert(String title, String content) {
        log.error("Showing alert: {} - {}", title, content);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearAndFillTableWithResponse(Response response) {
        log.info("Clearing and filling TableView with response: {}", response.getBody());
        Platform.runLater(() -> {
            products.clear();
            try {
                Object body = JsonUtils.fromJson(response.getBody(), Object.class);
                if (body instanceof List) {
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
                } else {
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
                }
                productTable.setItems(products);
                log.debug("TableView updated with products: {}", products);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse products from response: {}", response.getBody(), e);
                throw new RuntimeException("Failed to parse products", e);
            }
        });
    }
}