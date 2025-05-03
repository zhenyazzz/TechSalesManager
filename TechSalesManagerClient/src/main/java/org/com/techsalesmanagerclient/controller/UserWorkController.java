package org.com.techsalesmanagerclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
public class UserWorkController {

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
    public Button SearchButton;

    @FXML
    public Button submitButton;

    @FXML
    public TextField filterField;

    @FXML
    private Button ExitButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<?> sortComboBox;

    @FXML
    private TableView<Map<String, Object>> userTable;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
    private final NettyClient nettyClient = NettyClient.getInstance();
    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    private final List<String> expectedKeys = Arrays.asList("id", "name", "surname", "username", "email", "password", "role");

    public UserWorkController() throws InterruptedException {
    }

    @FXML
    public void initialize() throws IOException, TimeoutException {


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



        JsonMessage request = new JsonMessage();
        request.setCommand("get_users");
        request.addData("command","get_users");

        JsonMessage response = nettyClient.sendRequest(request);
        log.info(response.toString());
        updateTableView(extractUsersFromJsonMessage(response));

    }

    @FXML
    void handleExit(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Work_Menu.fxml",ExitButton);
    }

    @FXML
    void handleFilterAndSort(ActionEvent event) {

    }

    @FXML
    void handleSearch(ActionEvent event) throws IOException, TimeoutException {

        Long id = Long.valueOf(searchField.getText());

        JsonMessage request = new JsonMessage();
        request.setCommand("search_user");

        request.addData("id", id);
        JsonMessage response = nettyClient.sendRequest(request);
        if (response.getCommand().equals("success")) {
            clearAndFillTableWithSingleUser(response.getData());
            log.info(response.toString());
        }
        else {
            log.error(response.toString());
        }
    }

    private void clearAndFillTableWithSingleUser(Map<String, Object> user) {
        log.info("Clearing and filling TableView with single user: {}", user);
        Platform.runLater(() -> {
            users.clear();
            Map<String, Object> normalizedUser = normalizeUser(user);
            users.add(normalizedUser);
            userTable.setItems(users);
            log.debug("TableView updated with single user: {}", users);
        });
    }

    private void updateTableView(List<Map<String, Object>> response) {

        log.info("Updating TableView with {} items", response.size());
        List<Map<String, Object>> normalizedList = response.stream().map(this::normalizeMap).collect(Collectors.toList());
        Platform.runLater(() -> {
            users.clear();
            users.addAll(normalizedList);
            log.debug("TableView updated with: {}", normalizedList);
        });
        userTable.setItems(users);
    }

    private Map<String, Object> normalizeUser(Map<String, Object> input) {
        log.info("Normalizing user data: {}", input);

        // Извлекаем внутреннюю карту user
        Map<String, Object> userData = (Map<String, Object>) input.get("user");
        if (userData == null) {
            log.error("No 'user' key found in input: {}", input);
            throw new IllegalArgumentException("No 'user' key found in input");
        }

        // Нормализуем данные пользователя
        Map<String, Object> normalized = new HashMap<>();
        for (String key : expectedKeys) {
            normalized.put(key, userData.getOrDefault(key, null));
        }

        // Проверяем на лишние ключи
        if (!userData.keySet().stream().allMatch(expectedKeys::contains)) {
            List<String> extraKeys = userData.keySet().stream()
                    .filter(key -> !expectedKeys.contains(key))
                    .collect(Collectors.toList());
            log.warn("Found extra keys in user data: {}", extraKeys);
        }

        log.debug("Normalized user: {}", normalized);
        return normalized;
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

    public List<Map<String, Object>> extractUsersFromJsonMessage(JsonMessage message) {
        if (!"users".equals(message.getCommand())) {
            log.error("Invalid command: expected 'users', got '{}'", message.getCommand());
            throw new IllegalArgumentException("Invalid command: " + message.getCommand());
        }

        Object usersData = message.getData().get("users");
        if (usersData == null) {
            log.warn("No 'users' key found in message data: {}", message);
            return new ArrayList<>();
        }

        try {
            if (usersData instanceof String) {
                // Если данные пришли как JSON-строка
                List<Map<String, Object>> userList = mapper.readValue(
                        (String) usersData,
                        mapper.getTypeFactory().constructCollectionType(List.class, Map.class)
                );
                log.info("Extracted {} users from JSON string", userList.size());
                return userList;
            } else if (usersData instanceof List) {
                // Если данные уже десериализованы как List<Map>
                List<Map<String, Object>> userList = mapper.convertValue(
                        usersData,
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                log.info("Extracted {} users from JsonMessage", userList.size());
                return userList;
            } else {
                // Если данные пришли как объект (Map)
                log.warn("Unexpected data format for 'users': expected List or String, got {}", usersData.getClass());
                Map<String, Object> singleUser = mapper.convertValue(usersData, new TypeReference<Map<String, Object>>() {});
                List<Map<String, Object>> userList = new ArrayList<>();
                userList.add(singleUser);
                log.info("Converted single user object to list: {}", userList);
                return userList;
            }
        } catch (Exception e) {
            log.error("Failed to parse users from JsonMessage: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to parse users: " + e.getMessage(), e);
        }
    }

    private void showAlert(String title, String content) {
        log.error("Showing alert: {} - {}", title, content);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
