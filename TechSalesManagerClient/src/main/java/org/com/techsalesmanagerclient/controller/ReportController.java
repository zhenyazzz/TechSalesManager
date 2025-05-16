package org.com.techsalesmanagerclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.com.techsalesmanagerclient.client.Client;
import org.com.techsalesmanagerclient.client.JsonUtils;
import org.com.techsalesmanagerclient.client.Request;
import org.com.techsalesmanagerclient.client.Response;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.com.techsalesmanagerclient.enums.ResponseStatus;
import org.com.techsalesmanagerclient.model.Product;
import org.com.techsalesmanagerclient.service.ProductReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    @FXML
    public Button exitButton;


    private final WorkWithScenes workWithScenes = new WorkWithScenes();

    @FXML
    void initialize() {
        try {
            // Отправка запроса на сервер для получения списка товаров
            Request request = new Request(RequestType.GET_ALL_PRODUCTS, "");
            Response response = Client.send(request);

            if (response.getStatus() == ResponseStatus.Ok) {
                // Получение списка товаров
                System.out.println(response.getBody());
                List<Map<String, Object>> rawProducts = JsonUtils.fromJson(response.getBody(), List.class);
                List<Product> products = new ArrayList<>();
                for (Map<String, Object> rawProduct : rawProducts) {
                    products.add(new Product(rawProduct));
                }

                System.out.println(products);

                log.info("Products received");
                if (products != null && !products.isEmpty()) {
                    // Формирование имени файла отчета
                    LocalDateTime now = LocalDateTime.now();
                    String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    String reportFileName = "Product_Report_" + timestamp + ".pdf";

                    // Открытие новой вкладки для отображения графиков
                    ProductReportService reportGenerator = new ProductReportService();

                    // Откладываем выполнение до полной загрузки сцены
                    javafx.application.Platform.runLater(() -> {
                        try {
                            reportGenerator.showReport(products, reportFileName, exitButton);
                        } catch (Exception ex) {
                            log.error("Error generating report: {}", ex.getMessage());
                        }
                    });
                } else {
                    System.out.println("No products found for generating report.");
                }
            } else {
                System.out.println("Error fetching products: " + response.getBody());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public void handleExit(ActionEvent actionEvent) {
        workWithScenes.loadScene("/org/com/techsalesmanagerclient/Admin_Menu.fxml", exitButton);
    }
}
