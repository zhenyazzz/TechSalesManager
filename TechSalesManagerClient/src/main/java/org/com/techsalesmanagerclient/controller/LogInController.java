package org.com.techsalesmanagerclient.controller;



import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.com.techsalesmanagerclient.client.JsonMessage;
import org.com.techsalesmanagerclient.client.NettyClient;

public class LogInController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private PasswordField PasswordField;

    @FXML
    private Button logInButton;

    @FXML
    private TextField logInNicknameField;

    @FXML
    private Button signUpButton;
    @FXML
    private Text nicknameErrorMessage;

    @FXML
    private Text passwordErrorMessage;
    private final NettyClient nettyClient = new NettyClient();
    private final WorkWithScenes workWithScenes = new WorkWithScenes();

    public LogInController() throws InterruptedException {
    }

    @FXML
    void initialize() throws InterruptedException {


        signUpButton.setOnAction(event -> {
            workWithScenes.loadScene("/org/com/techsalesmanagerclient/singUp.fxml", signUpButton);

        });

        logInButton.setOnAction(event -> {
            try {
                passwordErrorMessage.setText("");
                nicknameErrorMessage.setText("");



                String nickname = logInNicknameField.getText().trim();
                String password = PasswordField.getText().trim();

                JsonMessage message = new JsonMessage();
                message.setCommand("login");
                message.addData("username", nickname);
                message.addData("password", password);


                JsonMessage response = nettyClient.sendRequest(message);


                if(response.getData().get("role").equals("admin")) {
                    System.out.println("admin");
                    //workWithScenes.loadScene("/org/com/techsalesmanagerclient/singUp.fxml", signUpButton);
                } else if (response.getData().get("role").equals("user")) {
                    System.out.println("user");
                    //workWithScenes.loadScene("/org/com/techsalesmanagerclient/singUp.fxml", signUpButton);
                }
                else {
                    System.out.println(response.getCommand());
                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
                //throw new RuntimeException("Ошибка отправки сообщения");
            }
        });
    }


    private void checkingAllFields(String nickname, String password) {
        int randomNumberOfMaxTrials = (new Random()).nextInt(8, 10);
        boolean isCorrectNickname = false;

    }
}
