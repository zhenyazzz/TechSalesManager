package org.com.techsalesmanagerclient.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class WorkWithScenes {

    public void loadScene(String fxmlFile, Node node) {
        try {
            // 1. Загружаем FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));

            // 2. Получаем текущее окно (Stage)
            Stage stage = (Stage) node.getScene().getWindow();

            // 3. Устанавливаем новую сцену
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Ошибка загрузки FXML: " + fxmlFile);
        }
    }
}