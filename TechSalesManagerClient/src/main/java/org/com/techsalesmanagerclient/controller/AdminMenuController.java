package org.com.techsalesmanagerclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerclient.client.Client;
import org.com.techsalesmanagerclient.client.JsonUtils;
import org.com.techsalesmanagerclient.client.Request;
import org.com.techsalesmanagerclient.client.Response;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.com.techsalesmanagerclient.enums.ResponseStatus;
import org.com.techsalesmanagerclient.model.Product;
import org.com.techsalesmanagerclient.service.ProductReportService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
public class AdminMenuController {

    @FXML
    public Button Create_Report_Button;

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
            workWithScenes.loadScene("/org/com/techsalesmanagerclient/Catalog.fxml", CatalogButton);
        });

        // Переход в меню работы с товарами
        ProductMenuWorkButton.setOnAction(event -> {
            workWithScenes.loadScene("/org/com/techsalesmanagerclient/Product_Work_Menu.fxml", ProductMenuWorkButton);
        });

        // Переход в меню работы с пользователями
        UserMenuWorkButton.setOnAction(event -> {
            workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Work_Menu.fxml", UserMenuWorkButton);
        });

        // Выход из системы
        ExitButton.setOnAction(event -> {

        });
    }

    @FXML
    private void Create_Report(ActionEvent event) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/ReportWindow.fxml", ExitButton);


    }
}