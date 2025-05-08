package org.com.techsalesmanagerclient.client;


import lombok.Data;
import org.com.techsalesmanagerclient.enums.Role;

import java.io.Serializable;

@Data
public class LoginResult implements Serializable {
    private Long id;
    private Role role;

}