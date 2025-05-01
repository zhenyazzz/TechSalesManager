package org.com.techsalesmanagerclient.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerclient.client.JsonMessage;
import org.com.techsalesmanagerclient.client.NettyClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
public class UserCrudController {

    @FXML
    private Button ExitButton;

    @FXML
    private Button createButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button updateButton;

    @FXML
    private TableView<Map<String, Object>> userTable;

    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TableColumn<Map<String, Object>, Number> idColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> nameColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> surnameColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> usernameColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> emailColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> passwordColumn;

    @FXML
    private TableColumn<Map<String, Object>, String> roleColumn;

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField usernameFieldSide;

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
    private final NettyClient nettyClient = NettyClient.getInstance();
    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    private final List<String> expectedKeys = Arrays.asList("id", "name", "surname", "username", "email", "password", "role");

    public UserCrudController() throws InterruptedException {
    }

    @FXML
    void initialize() throws IOException, TimeoutException {
        roleComboBox.getItems().add("CUSTOMER");
        roleComboBox.getItems().add("ADMIN");

        // Настройка CellValueFactory для каждой колонки
        idColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("id");
            if (value instanceof Number) {
                return new SimpleObjectProperty<>((Number) value);
            } else if (value == null) {
                return new SimpleObjectProperty<>(null);
            } else {
                try {
                    return new SimpleObjectProperty<>(Integer.parseInt(value.toString()));
                } catch (NumberFormatException e) {
                    log.warn("Invalid number format for id: {}", value);
                    return new SimpleObjectProperty<>(null);
                }
            }
        });

        nameColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("name");
            return new SimpleObjectProperty<>(value != null ? value.toString() : null);
        });

        surnameColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("surname");
            return new SimpleObjectProperty<>(value != null ? value.toString() : null);
        });

        usernameColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("username");
            return new SimpleObjectProperty<>(value != null ? value.toString() : null);
        });

        emailColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("email");
            return new SimpleObjectProperty<>(value != null ? value.toString() : null);
        });

        passwordColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("password");
            return new SimpleObjectProperty<>(value != null ? value.toString() : null);
        });

        roleColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("role");
            return new SimpleObjectProperty<>(value != null ? value.toString() : null);
        });

        JsonMessage message = new JsonMessage();
        message.setCommand("get_users");
        message.addData("command","get_users");



       // JsonMessage response = nettyClient.sendRequest(message);
        List<Map<String, Object>> response = nettyClient.sendListRequest(message);
        System.out.println(response.toString());
        updateTableView(response);
        userTable.setItems(users);




    }





    @FXML
    void handleCreate(ActionEvent event) {

    }

    @FXML
    void handleDelete(ActionEvent event) {

    }

    @FXML
    void handleExit(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Work_Menu.fxml",ExitButton);
    }

    @FXML
    void handleUpdate(ActionEvent event) {

    }


    private void updateTableView(List<Map<String, Object>> response) {
        log.info("Updating TableView with {} items", response.size());
        List<Map<String, Object>> normalizedList = response.stream().map(this::normalizeMap).collect(Collectors.toList());
        Platform.runLater(() -> {
            users.clear();
            users.addAll(normalizedList);
            log.debug("TableView updated with: {}", normalizedList);
        });
    }

    private Map<String, Object> normalizeMap(Map<String, Object> input) {
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
}
