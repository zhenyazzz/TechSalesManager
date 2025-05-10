package org.com.techsalesmanagerclient.model;

import lombok.Builder;
import lombok.Data;
import org.com.techsalesmanagerclient.enums.Role;

import java.util.List;

@Data
public class User {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private Role role;

    private List<Object> orders=null;



}
