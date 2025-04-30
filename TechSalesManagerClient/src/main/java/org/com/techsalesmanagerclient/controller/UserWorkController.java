package org.com.techsalesmanagerclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserWorkController {

    @FXML
    private Button ExitButton;

    @FXML
    private TableColumn<?, ?> emailColumn;

    @FXML
    private ComboBox<?> filterComboBox;

    @FXML
    private TableColumn<?, ?> idColumn;

    @FXML
    private TableColumn<?, ?> nameColumn;

    @FXML
    private TableColumn<?, ?> roleColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<?> sortComboBox;

    @FXML
    private TableView<?> userTable;

    private final WorkWithScenes workWithScenes = new WorkWithScenes();

    @FXML
    void handleExit(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Work_Menu.fxml",ExitButton);
    }

    @FXML
    void handleFilterAndSort(ActionEvent event) {

    }

    @FXML
    void handleSearch(ActionEvent event) {

    }
}
