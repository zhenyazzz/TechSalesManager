package org.com.techsalesmanagerclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerclient.client.JsonUtils;
import org.com.techsalesmanagerclient.client.Request;
import org.com.techsalesmanagerclient.client.Response;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.com.techsalesmanagerclient.enums.ResponseStatus;
import org.com.techsalesmanagerclient.enums.Role;
import org.com.techsalesmanagerclient.model.POJO_User;
import org.com.techsalesmanagerclient.model.User;
import org.jetbrains.annotations.NotNull;
import org.com.techsalesmanagerclient.client.Client;

import java.io.IOException;
import java.sql.ClientInfoStatus;
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
    private TableView<POJO_User> userTable;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TableColumn<POJO_User, Number> idColumn;

    @FXML
    private TableColumn<POJO_User, String> nameColumn;

    @FXML
    private TableColumn<POJO_User, String> surnameColumn;

    @FXML
    private TableColumn<POJO_User, String> usernameColumn;

    @FXML
    private TableColumn<POJO_User, String> emailColumn;

    @FXML
    private TableColumn<POJO_User, String> passwordColumn;

    @FXML
    private TableColumn<POJO_User, String> roleColumn;

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
    private final ObservableList<POJO_User> users = FXCollections.observableArrayList();
   
    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    private final List<String> expectedKeys = Arrays.asList("id", "name", "surname", "username", "email", "password", "role");

    public UserCrudController() throws InterruptedException {
    }

    @FXML
    void initialize() throws IOException, TimeoutException, ClassNotFoundException {
        roleComboBox.getItems().add("CUSTOMER");
        roleComboBox.getItems().add("ADMIN");

        // Настройка столбцов
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        surnameColumn.setCellValueFactory(cellData -> cellData.getValue().surnameProperty());
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        passwordColumn.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
      
        // Добавляем слушатель для выбора записи в TableView
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearInputFields();
            }
        });

        Request request = new Request(RequestType.GET_ALL_USERS,"");

        Response response = Client.send(request);
        log.info(response.toString());
        List<Map<String, Object>> rawUsers=JsonUtils.fromJson(response.getBody(),List.class);
        // Преобразование LinkedHashMap в POJO_User
        for (Map<String, Object> rawUser : rawUsers) {
            users.add(new POJO_User(
                    ((Number) rawUser.get("id")).longValue(), // Преобразование id в Long
                    (String) rawUser.get("name"),
                    (String) rawUser.get("surname"),
                    (String) rawUser.get("username"),
                    (String) rawUser.get("email"),
                    (String) rawUser.get("password"),
                    (String) rawUser.get("role")
            ));
        }

        updateTableView(users);

      /* // Request response = nettyClient.sendRequest(message);
        List<Map<String, Object>> response = nettyClient.sendListRequest(message);
        System.out.println(response.toString());
        updateTableView(response);*/


    }

    @FXML
    void handleSearch(ActionEvent event) throws IOException, TimeoutException, ClassNotFoundException {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_CRUD.fxml",createButton);
    }


    @FXML
    void handleCreate(ActionEvent event) throws IOException, TimeoutException, ClassNotFoundException {

        String name = nameField.getText();
        String surname = surnameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String role = roleComboBox.getValue();

        Map<String, Object> data = new HashMap<>();
        data.put("name",name);
        data.put("surname",surname);
        data.put("username", username);
        data.put("email",email);
        data.put("password", password);
        data.put("role", role);


        Request request = new Request(RequestType.CREATE_USER, mapToJson(data));

        Response response = Client.send(request);
        log.info(response.toString());
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_CRUD.fxml",createButton);
        //handleSearch(event);
    }

    @FXML
    void handleDelete(ActionEvent event) {
        try {
            // Проверка, что пользователь выбран
            if (userTable.getSelectionModel().getSelectedItem() == null) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите пользователя для удаления"));
                return;
            }

            // Получаем ID из поля ввода
            String idText = idField.getText();
            if (idText == null || idText.isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите пользователя для удаления"));
                return;
            }

            Long userId;
            try {
                userId = Long.parseLong(idText);
            } catch (NumberFormatException e) {
                log.error("Invalid ID format: {}", idText);
                Platform.runLater(() -> showAlert("Error", "Неверный формат ID: " + idText));
                return;
            }

            // Создаём JSON с id (просто строка с числом)
            String json = mapper.writeValueAsString(userId); // Преобразуем Long в строку, например "3"
            Request request = new Request(RequestType.DELETE_USER, json);
            log.debug("Sending delete request: {}", request);

            // Отправляем запрос
            Response response = Client.send(request);
            log.info("Delete response: {}", response);

            // Обрабатываем ответ
            if (response.getStatus() == ResponseStatus.Ok) {
                Platform.runLater(() -> {
                    // Удаляем пользователя из TableView
                    users.removeIf(user -> user.getId() != null && user.getId().equals(userId));
                    // Явно обновляем TableView
                    userTable.setItems(FXCollections.observableArrayList(users));
                    clearInputFields();
                    log.info("Removed user with ID {} from TableView", userId);
                    // Опционально: полная синхронизация с сервером
                    try {
                        handleSearch(event);
                    } catch (Exception e) {
                        log.error("Failed to refresh table after delete", e);
                    }
                });
            } else {
                Platform.runLater(() -> showAlert("Error", "Не удалось удалить пользователя: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to delete user", e);
            Platform.runLater(() -> showAlert("Error", "Не удалось удалить пользователя: " + e.getMessage()));
        }
    }

    @FXML
    void handleExit(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Work_Menu.fxml",ExitButton);
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        try {
            // Проверка, что пользователь выбран
            if (userTable.getSelectionModel().getSelectedItem() == null) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите пользователя для обновления"));
                return;
            }

            // Получаем ID из поля ввода
            String idText = idField.getText();
            if (idText == null || idText.isEmpty()) {
                Platform.runLater(() -> showAlert("Warning", "Пожалуйста, выберите пользователя для обновления"));
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
            String surname = surnameField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            String role = roleComboBox.getValue();

            if (name.isEmpty() || surname.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
                Platform.runLater(() -> showAlert("Warning", "Все поля должны быть заполнены"));
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", name);
            data.put("surname", surname);
            data.put("username", username);
            data.put("email", email);
            data.put("password", password);
            data.put("role", role);

            Request request = new Request(RequestType.UPDATE_USER, mapToJson(data));
            log.debug("Sending update request: {}", request);

            Response response = Client.send(request);
            log.info("Update response: {}", response);

            if (response.getStatus() == ResponseStatus.Ok) {
                Platform.runLater(() -> {
                    clearInputFields();
                    try {
                        handleSearch(event); // Обновляем таблицу
                    } catch (Exception e) {
                        log.error("Failed to refresh table after update", e);
                    }
                });
            } else {
                Platform.runLater(() -> showAlert("Error", "Не удалось обновить пользователя: " + response.getBody()));
            }
        } catch (Exception e) {
            log.error("Failed to update user", e);
            Platform.runLater(() -> showAlert("Error", "Не удалось обновить пользователя: " + e.getMessage()));
        }
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_CRUD.fxml",createButton);
    }


    private void updateTableView(List<POJO_User> users) {
        /*log.info("Updating TableView with {} items", response.size());
        List<Map<String, Object>> normalizedList = response.stream().map(this::normalizeMap).collect(Collectors.toList());
        Platform.runLater(() -> {
            users.clear();
            users.addAll(normalizedList);
            log.debug("TableView updated with: {}", normalizedList);
        });*/
        ObservableList<POJO_User> observableNames = FXCollections.observableArrayList(users);
        userTable.setItems(observableNames);
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
    private Request getRequest() throws JsonProcessingException {
        String name = nameField.getText();
        String surname = surnameField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String role = roleComboBox.getValue();
        Map<String, Object> data = new HashMap<>();

        data.put("name",name);
        data.put("surname",surname);
        data.put("username", username);
        data.put("email",email);
        data.put("password", password);
        data.put("role", role);
        Request request = new Request(RequestType.CREATE_USER, JsonUtils.toJson(data));
        return request;
    }

/*    public List<Map<String, Object>> extractUsersFromRequest(Request message) {
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
                log.info("Extracted {} users from Request", userList.size());
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
            log.error("Failed to parse users from Request: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to parse users: " + e.getMessage(), e);
        }
    }*/

    private void populateFields(POJO_User user) {
        idField.setText(user.getId() != null ? user.getId().toString() : "");
        nameField.setText(user.getName() != null ? user.getName() : "");
        surnameField.setText(user.getSurname() != null ? user.getSurname() : "");
        usernameField.setText(user.getUsername() != null ? user.getUsername() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        passwordField.setText(user.getPassword() != null ? user.getPassword() : "");
        roleComboBox.setValue(user.getRole() != null ? user.getRole() : null);
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

    public static String mapToJson(Map<String, Object> data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);
            log.info("Converted Map to JSON: {}", json);
            return json;
        } catch (Exception e) {
            log.error("Failed to convert Map to JSON: {}", data, e);
            throw new RuntimeException("Failed to convert Map to JSON", e);
        }
    }
}
