package org.com.techsalesmanagerclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.com.techsalesmanagerclient.client.Client;
import org.com.techsalesmanagerclient.client.JsonUtils;
import org.com.techsalesmanagerclient.client.Request;
import org.com.techsalesmanagerclient.client.Response;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.com.techsalesmanagerclient.enums.ResponseStatus;
import org.com.techsalesmanagerclient.model.POJO_User;
import org.com.techsalesmanagerclient.model.User;
import org.com.techsalesmanagerclient.service.UserFilterService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
public class UserWorkController {

    @FXML
    public ComboBox<String> filterComboBox;

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
    private TableView<POJO_User> userTable;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ObservableList<POJO_User> users = FXCollections.observableArrayList();

    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    private final List<String> expectedKeys = Arrays.asList("id", "name", "surname", "username", "email", "password", "role");

    public UserWorkController() throws InterruptedException {
    }

    @FXML
    public void initialize() throws IOException, TimeoutException, ClassNotFoundException {
        filterComboBox.getItems().add("По ID");
        filterComboBox.getItems().add("По почте");

        // Настройка столбцов
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        surnameColumn.setCellValueFactory(cellData -> cellData.getValue().surnameProperty());
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        passwordColumn.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());




        Request request = new Request(RequestType.GET_ALL_USERS,"");


        Response response = Client.send(request);
        log.info(response.toString());
        List<Map<String, Object>> rawUsers= JsonUtils.fromJson(response.getBody(),List.class);
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

    }

    @FXML
    void handleExit(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Work_Menu.fxml",ExitButton);
    }

    @FXML
    void handleFilterAndSort(ActionEvent event) {
        try {
            String filterType = filterComboBox.getValue();
            String filterValue = filterField.getText();
            Response response = UserFilterService.filter(filterType, filterValue);
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
    void handleSearch(ActionEvent event) throws IOException, TimeoutException, ClassNotFoundException {

        Long id = Long.valueOf(searchField.getText());

        Map<String, Object> data = new HashMap<>();
        Request request = new Request(RequestType.SEARCH_USER,id.toString());
        Response response = Client.send(request);

        if (response.getStatus().equals(ResponseStatus.Ok)) {
            clearAndFillTableWithSingleUser(response);
            log.info(response.toString());
        }
        else {
            log.error(response.toString());
        }
    }

    private void clearAndFillTableWithSingleUser(Response response) {
       log.info("Clearing and filling TableView with single user: {}", response.getBody());
        Platform.runLater(() -> {
            users.clear();
           // Map<String, Object> normalizedUser = normalizeUser(user);
            try {
                User user=JsonUtils.fromJson(response.getBody(), User.class);
                POJO_User pojo_user=new POJO_User(user);
                users.add(pojo_user);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            userTable.setItems(users);
            log.debug("TableView updated with single user: {}", users);
        });
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
            users.clear();
            try {
                // Проверяем, является ли ответ списком или одиночным пользователем
                Object body = JsonUtils.fromJson(response.getBody(), Object.class);
                if (body instanceof List) {
                    List<Map<String, Object>> rawUsers = JsonUtils.fromJson(response.getBody(), List.class);
                    for (Map<String, Object> rawUser : rawUsers) {
                        users.add(new POJO_User(
                                ((Number) rawUser.get("id")).longValue(),
                                (String) rawUser.get("name"),
                                (String) rawUser.get("surname"),
                                (String) rawUser.get("username"),
                                (String) rawUser.get("email"),
                                (String) rawUser.get("password"),
                                (String) rawUser.get("role")
                        ));
                    }
                } else {
                    // Одиночный пользователь
                    User user = JsonUtils.fromJson(response.getBody(), User.class);
                    users.add(new POJO_User(user));
                }
                userTable.setItems(users);
                log.debug("TableView updated with users: {}", users);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse users from response: {}", response.getBody(), e);
                throw new RuntimeException("Failed to parse users", e);
            }
        });
    }
}
