package org.com.techsalesmanagerclient.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingUpForm {
    private String name;
    private String surname;
    private String nickname;
    private String password;
    private String email;
}
