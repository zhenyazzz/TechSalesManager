package org.com.techsalesmanagerserver.server;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.com.techsalesmanagerserver.model.Role;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AuthorizationResult implements Serializable {
    private Long id;
    private Role role;

}