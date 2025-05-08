package org.com.techsalesmanagerclient;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.com.techsalesmanagerclient.client.Client;

import java.io.IOException;

public class ApplicationLoader extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Client client = Client.getInstance();
        if (client != null) {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLoader.class.getResource("/org/com/techsalesmanagerclient/logIn.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Monobank!");
        stage.setScene(scene);
        stage.show();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
