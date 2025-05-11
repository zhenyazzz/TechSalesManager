package org.com.techsalesmanagerserver.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.com.techsalesmanagerserver.model.Role;


import java.io.Serializable;

@Data
@AllArgsConstructor
public class LoginResult implements Serializable {
    private Long id;
    private Role role;

}