package org.com.techsalesmanagerclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class UserWorkMenuController {
    @FXML
    private Button ExitButton;

    @FXML
    private Button UserCrudButton;

    @FXML
    private Button UserWorkButton;

    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    // workWithScenes.loadScene("/org/com/techsalesmanagerclient/singUp.fxml", signUpButton);

    @FXML
    void initialize() {
        // Переход в каталог
       UserCrudButton.setOnAction(event -> {
            workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_CRUD.fxml",UserCrudButton);
        });

        // Переход в меню работы с товарами
        UserWorkButton.setOnAction(event -> {
            workWithScenes.loadScene( "/org/com/techsalesmanagerclient/User_work.fxml",UserWorkButton);
        });

        // Выход из системы
        ExitButton.setOnAction(event -> {
            workWithScenes.loadScene( "/org/com/techsalesmanagerclient/logIn.fxml",ExitButton);
        });
    }
}
