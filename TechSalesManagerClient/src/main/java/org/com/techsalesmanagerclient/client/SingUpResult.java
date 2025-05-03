package org.com.techsalesmanagerclient.client;

import lombok.Data;
import org.com.techsalesmanagerclient.enums.Role;

@Data
public class SingUpResult {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private Role role;
}
