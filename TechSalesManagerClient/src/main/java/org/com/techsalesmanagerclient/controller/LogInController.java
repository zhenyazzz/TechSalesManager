package org.com.techsalesmanagerclient.controller;



import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import org.com.techsalesmanagerclient.client.*;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.com.techsalesmanagerclient.enums.Role;

@Slf4j
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

                LoginForm loginForm = new LoginForm();
                loginForm.setLogin(nickname);
                loginForm.setPassword(password);

                Request request = new Request(RequestType.AUTHORIZATION, JsonUtils.toJson(loginForm));

                Response response =  Client.send(request);

                LoginResult result = JsonUtils.fromJson(response.getBody(), LoginResult.class);

                if(result.getRole().equals(Role.ADMIN)) {
                    System.out.println("admin");
                    workWithScenes.loadScene("/org/com/techsalesmanagerclient/Admin_Menu.fxml", signUpButton);
                } else if (result.getRole().equals(Role.CUSTOMER)) {
                    System.out.println("user");
                    workWithScenes.loadScene("/org/com/techsalesmanagerclient/User_Menu.fxml", signUpButton);
                }
                else {
                    System.out.println(response);
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }


    private void checkingAllFields(String nickname, String password) {
        int randomNumberOfMaxTrials = (new Random()).nextInt(8, 10);
        boolean isCorrectNickname = false;

    }
}
