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
import javafx.scene.control.cell.MapValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerclient.client.JsonMessage;
import org.com.techsalesmanagerclient.client.NettyClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
public class UserCrudController {

    @FXML
    public Button SearchButton;

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
    private TextField usernameField;

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

        // Добавляем слушатель для выбора записи в TableView
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearInputFields();
            }
        });

        JsonMessage request = new JsonMessage();
        request.setCommand("get_users");
        request.addData("command","get_users");

        JsonMessage response = nettyClient.sendRequest(request);
        log.info(response.toString());
        updateTableView(extractUsersFromJsonMessage(response));

      /* // JsonMessage response = nettyClient.sendRequest(message);
        List<Map<String, Object>> response = nettyClient.sendListRequest(message);
        System.out.println(response.toString());
        updateTableView(response);*/
       




    }

    @FXML
    void handleSearch(ActionEvent event) throws IOException, TimeoutException {
        JsonMessage request = new JsonMessage();
        request.setCommand("get_users");
        request.addData("command","get_users");



        // JsonMessage response = nettyClient.sendRequest(message);
        //List<Map<String, Object>> response = nettyClient.sendListRequest(message);
        JsonMessage response = nettyClient.sendRequest(request);

        updateTableView(extractUsersFromJsonMessage(response));
        userTable.setItems(users);
        
        
    }


    @FXML
    void handleCreate(ActionEvent event) throws IOException, TimeoutException {

        String name = nameField.getText();
        String surname = surnameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String role = roleComboBox.getValue();
        JsonMessage request = new JsonMessage();
        request.setCommand("create_user");
        request.addData("name",name);
        request.addData("surname",surname);
        request.addData("username", username);
        request.addData("email",email);
        request.addData("password", password);
        request.addData("role", role);
        JsonMessage response = nettyClient.sendRequest(request);
        log.info(response.toString());
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_CRUD.fxml",createButton);
        //handleSearch(event);
    }

    @FXML
    void handleDelete(ActionEvent event) {
        try {
            // Получаем ID из поля ввода (заполняется при выборе записи в TableView)
            String idText = idField.getText();
            if (idText == null || idText.isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите пользователя для удаления"));
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(idText);
            } catch (NumberFormatException e) {
                log.error("Invalid ID format: {}", idText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат ID: " + idText));
                return;
            }

            // Создаём запрос на удаление
            JsonMessage request = new JsonMessage();
            request.setCommand("delete_user");
            request.addData("id", userId);
            log.debug("Sending delete request: {}", request);

            // Отправляем запрос на сервер
            JsonMessage response = nettyClient.sendRequest(request);
            log.debug("Received response: {}", response);

            // Обрабатываем ответ от сервера
            if ("success".equals(response.getCommand())) {


                // Удаляем из TableView
                Platform.runLater(() -> {
                    users.removeIf(user -> {
                        Object id = user.get("id");
                        return id != null && id.toString().equals(String.valueOf(userId));
                    });
                    log.info("Removed user with ID {} from TableView", userId);
                    clearInputFields();
                });
            } else if ("error".equals(response.getCommand())) {
                String reason = response.getData().toString();
                log.error("Server error: {}", reason);
                Platform.runLater(() -> showAlert("Error", "Ошибка сервера: " + reason));
            } else {
                log.error("Unexpected response command: {}", response.getCommand());
                Platform.runLater(() -> showAlert("Error", "Неожиданный ответ от сервера: " + response.getCommand()));
            }
        } catch (Exception e) {
            log.error("Failed to delete user: {}", e.getMessage(), e);
            Platform.runLater(() -> showAlert("Error", "Не удалось удалить пользователя: " + e.getMessage()));
        }
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
        userTable.setItems(users);
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

    @NotNull
    private JsonMessage getJsonMessage() {
        String name = nameField.getText();
        String surname = surnameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String role = roleComboBox.getValue();
        JsonMessage request = new JsonMessage();
        request.setCommand("create_user");
        request.addData("name",name);
        request.addData("surname",surname);
        request.addData("username", username);
        request.addData("email",email);
        request.addData("password", password);
        request.addData("role", role);
        return request;
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

    private void populateFields(Map<String, Object> user) {
        idField.setText(user.get("id") != null ? user.get("id").toString() : "");
        nameField.setText(user.get("name") != null ? user.get("name").toString() : "");
        surnameField.setText(user.get("surname") != null ? user.get("surname").toString() : "");
        usernameField.setText(user.get("username") != null ? user.get("username").toString() : "");
        emailField.setText(user.get("email") != null ? user.get("email").toString() : "");
        passwordField.setText(user.get("password") != null ? user.get("password").toString() : "");
        roleComboBox.setValue(user.get("role") != null ? user.get("role").toString() : null);
        log.info("Populated fields with user data: {}", user);
    }

    private void clearInputFields() {
        idField.clear();
        nameField.clear();
        surnameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
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
