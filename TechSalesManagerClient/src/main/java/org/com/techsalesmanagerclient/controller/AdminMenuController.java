package org.com.techsalesmanagerclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;


public class AdminMenuController {


    @FXML
    private Button CatalogButton;

    @FXML
    private Button ExitButton;

    @FXML
    private Button ProductMenuWorkButton;

    @FXML
    private Button UserMenuWorkButton;

    private final WorkWithScenes workWithScenes = new WorkWithScenes();
   // workWithScenes.loadScene("/org/com/techsalesmanagerclient/singUp.fxml", signUpButton);

    @FXML
    void initialize() {
        // Переход в каталог
        CatalogButton.setOnAction(event -> {
            workWithScenes.loadScene("org/com/techsalesmanagerclient/Catalog.fxml",CatalogButton);
        });

        // Переход в меню работы с товарами
        ProductMenuWorkButton.setOnAction(event -> {
            workWithScenes.loadScene( "org/com/techsalesmanagerclient/Product_Work_Menu.fxml",ProductMenuWorkButton);
        });

        // Переход в меню работы с пользователями
        UserMenuWorkButton.setOnAction(event -> {
            workWithScenes.loadScene("org/com/techsalesmanagerclient/User_Work_Menu.fxml",UserMenuWorkButton);
        });

        // Выход из системы
        ExitButton.setOnAction(event -> {
            workWithScenes.loadScene( "org/com/techsalesmanagerclient/logIn.fxml",ExitButton);
        });
    }



}
