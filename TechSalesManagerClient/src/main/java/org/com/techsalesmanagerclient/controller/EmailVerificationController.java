package org.com.techsalesmanagerclient.controller;



import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class EmailVerificationController{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button backToSignUpButton;

    @FXML
    private TextField signUpNickname;

    @FXML
    private Button signUpSignUpButton;

    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    @FXML
    void initialize() {
        backToSignUpButton.setOnAction(action -> workWithScenes.loadScene("signUpWindow.fxml", backToSignUpButton));
        int verificationNumber = (new Random()).nextInt(100000, 999999);
        System.out.println(verificationNumber);
        signUpSignUpButton.setOnAction(action -> {
            System.out.println(verificationNumber);
            if (Integer.parseInt(signUpNickname.getText()) == verificationNumber) {
                workWithScenes.loadScene("/org/com/techsalesmanagerclient/controller/User_Menu.java", signUpSignUpButton);
            }
        });
    }

}
