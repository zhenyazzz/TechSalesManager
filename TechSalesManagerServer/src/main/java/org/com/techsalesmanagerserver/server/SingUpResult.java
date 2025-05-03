package org.com.techsalesmanagerserver.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.com.techsalesmanagerserver.model.Role;

@Data
@AllArgsConstructor
public class SingUpResult {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private Role role;
}
