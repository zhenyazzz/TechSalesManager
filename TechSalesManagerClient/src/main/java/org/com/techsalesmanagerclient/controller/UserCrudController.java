package org.com.techsalesmanagerclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    private TableView<?> userTable;

    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    void initialize() {
        roleComboBox.getItems().add("CUSTOMER");
        roleComboBox.getItems().add("ADMIN");
        // Настраиваем направление открытия списка


    }


    private final WorkWithScenes workWithScenes = new WorkWithScenes();
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
}
