package org.com.techsalesmanagerclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ProductWorkMenuController {
    @FXML
    private Button ExitButton;

    @FXML
    private Button ProductCrudButton;

    @FXML
    private Button ProductWorkButton;

    private final WorkWithScenes workWithScenes = new WorkWithScenes();

    @FXML
    public void initialize() {}


    public void handleCRUD(ActionEvent actionEvent) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/Product_CRUD.fxml",ExitButton);
    }

    public void handleWork(ActionEvent actionEvent) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/Product_work.fxml",ExitButton);
    }

    public void handleExit(ActionEvent actionEvent) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/logIn.fxml",ExitButton);
    }
}
