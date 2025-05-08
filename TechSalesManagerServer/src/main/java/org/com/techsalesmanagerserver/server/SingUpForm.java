package org.com.techsalesmanagerserver.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingUpForm {
    private String name;
    private String surname;
    private String nickname;
    private String password;
    private String email;
}
