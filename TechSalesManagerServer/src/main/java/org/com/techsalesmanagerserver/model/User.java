package org.com.techsalesmanagerserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'CUSTOMER'")
    @Builder.Default
    private Role role = Role.CUSTOMER;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;
}
