package org.com.techsalesmanagerclient.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringJoiner;
import java.util.concurrent.TimeoutException;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.com.techsalesmanagerclient.client.*;
import org.com.techsalesmanagerclient.enums.RequestType;
import org.com.techsalesmanagerclient.enums.ResponseStatus;
import org.jetbrains.annotations.NotNull;


public class SignUpController implements SignUpInformation {

    @FXML
    private Text emailErrorMessage;

    @FXML
    private Text globalErrorsText;

    @FXML
    private Text nameErrorMessage;

    @FXML
    private Text nicknameErrorMessage;

    @FXML
    private Text passwordErrorMessage;

    @FXML
    private TextField signUpEmail;

    @FXML
    private Button signUpLogInButton;

    @FXML
    private TextField signUpName;

    @FXML
    private TextField signUpNickname;

    @FXML
    private PasswordField signUpPassword;

    @FXML
    private Button signUpSignUpButton;

    @FXML
    private TextField signUpSurname;

    @FXML
    private Text surnameErrorMessage;



    private final WorkWithScenes workWithScenes = new WorkWithScenes();
    private final LinkedList<String> listOfEmptyFields = new LinkedList<>();

    public SignUpController() throws InterruptedException {
    }


    @FXML
    void initialize() {
        signUpLogInButton.setOnAction(event -> {
            workWithScenes.loadScene("/org/com/techsalesmanagerclient/logIn.fxml", signUpLogInButton);
        });

        signUpSignUpButton.setOnAction(eventForSignUpButtonInSignUpWindow -> {
            setToDefaultFields(signUpName, signUpNickname, signUpSurname, signUpPassword, signUpEmail);
            setToDefaultTextErrorsMessages(nameErrorMessage, surnameErrorMessage, nicknameErrorMessage, passwordErrorMessage, globalErrorsText, emailErrorMessage);
            SingUpForm singUpForm = createSingUpForm();

            System.out.println("Отправка данных на сервер ");
            try {
                Request request = new Request(RequestType.REGISTRATION, JsonUtils.toJson(singUpForm));

                Response response =  Client.send(request);

                SingUpResult result = JsonUtils.fromJson(response.getBody(), SingUpResult.class);
                if (response.getStatus().equals(ResponseStatus.Ok)){
                    workWithScenes.loadScene("/org/com/techsalesmanagerclient/emailVerification.fxml", signUpLogInButton);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull
    private SingUpForm createSingUpForm() {
        return new SingUpForm(
                signUpName.getText(),
                signUpSurname.getText(),
                signUpNickname.getText(),
                signUpPassword.getText(),
                signUpEmail.getText()
        );
    }

    private boolean checkAllParameters(String name, String surname, String nickname, String password, String email) {
        CorrectStructure correctStructure = new CorrectStructure();
        boolean nameBoolean = fieldsTest(name, minSymbolsNameField, maxSymbolsNameField, correctStructure, nameErrorMessage, FieldName.NAME, signUpName);
        boolean surnameBoolean = fieldsTest(surname, minSymbolsSurnameField, maxSymbolsSurnameField, correctStructure, surnameErrorMessage, FieldName.SURNAME, signUpSurname);
        boolean nicknameBoolean = fieldsTest(nickname, minSymbolsNicknameField, maxSymbolsNicknameField, correctStructure, nicknameErrorMessage, FieldName.NICKNAME, signUpNickname);
        boolean passwordBoolean = fieldsTest(password, minSymbolsPasswordField, maxSymbolsPasswordField, correctStructure, passwordErrorMessage, FieldName.PASSWORD, signUpPassword);
        boolean emailBoolean = fieldsTest(email, minSymbolsEmailField, maxSymbolsEmailField, correctStructure, emailErrorMessage, FieldName.EMAIL, signUpEmail);
        boolean errorMessages = testingListWithEmptyErrorMessages();
        if (!nameBoolean || !nicknameBoolean || !surnameBoolean || !passwordBoolean || !emailBoolean || !errorMessages) {
            return false;
        }
        return true;
    }

    private boolean fieldsTest(String word, int minAmountOfSymbols, int maxAmountOFSymbols, CorrectStructure correctStructure, Text textTable, FieldName fieldName,
                               TextField field) {
        boolean isOK = true;
        if(word.isEmpty()) {
            listOfEmptyFields.add(fieldName.getName());
            isOK = false;
        } else if (correctStructure.getMaxSortedSymbols(maxAmountOFSymbols, word)) {
            textTable.setText("You must have " + maxAmountOFSymbols + " symbols at max");
            isOK = false;
        } else if (correctStructure.getMinSortedSymbols(minAmountOfSymbols, word)) {
            textTable.setText("You must have " + minAmountOfSymbols + " symbols at least");
            isOK = false;
        }
        if (fieldName == FieldName.NAME || fieldName == FieldName.SURNAME) {
            if (correctStructure.containsSpace(word) && correctStructure.containsSpecialSymbols(word)) {
                textTable.setText("You have an extra space and special symbols");
                isOK = false;
            } else if (correctStructure.containsSpace(word)) {
                textTable.setText("You have an extra space");
                isOK = false;
            } else if (correctStructure.containsSpecialSymbols(word)) {
                textTable.setText("You have special symbols");
                isOK = false;
            }
        }
        if (fieldName == FieldName.NICKNAME || fieldName == FieldName.PASSWORD || fieldName == FieldName.EMAIL) {
            if (correctStructure.containsSpace(word)) {
                textTable.setText("You have an extra space");
                isOK = false;
            }
        }
        if (!isOK) {
            field.setStyle("-fx-border-color: #ff0000");
            return false;
        }
        return true;
    }

    private boolean testingListWithEmptyErrorMessages() {
        try {
            if (listOfEmptyFields.isEmpty()) {
                return true;
            } else if (listOfEmptyFields.size() == 1) {
                globalErrorsText.setText("The Field '" + listOfEmptyFields.getFirst() + "' is empty!!!");
            } else if (listOfEmptyFields.size() >= 4) {
                globalErrorsText.setText("Every field is empty!!!");
            } else {
                StringJoiner joiner = new StringJoiner(", ");
                listOfEmptyFields.forEach(joiner::add);
                globalErrorsText.setText("The Fields '" + joiner.toString() + "' are empty!!!");
            }
            return false;
        } finally {
            listOfEmptyFields.clear();
        }
    }

    private boolean theSameSurnameAndName(String name, String surname) {
        if(name.equals(surname) && !name.isEmpty() && !surname.isEmpty()) {
            globalErrorsText.setText("The fields Name and Surname are the same!");
            signUpName.setStyle("-fx-border-color: #ff0000");
            signUpSurname.setStyle("-fx-border-color: #ff0000");
            return true;
        }
        return false;
    }

    private void setToDefaultFields(TextField... fields) {
        Arrays.stream(fields).forEach(field -> field.setStyle(""));
    }

    private void setToDefaultTextErrorsMessages(Text... texts) {
        Arrays.stream(texts).forEach(text -> text.setText(""));
    }

    public enum FieldName {
        NAME{
            public String getName() {
                return "Name";
            }
        },
        SURNAME {
            public String getName() {
                return "Surname";
            }
        },
        NICKNAME {
            public String getName() {
                return "Nickname";
            }
        },
        PASSWORD {
            public String getName() {
                return "Password";
            }
        },
        EMAIL {
            public String getName() {
                return "Email";
            }
        };
        public abstract String getName();
    }
}
