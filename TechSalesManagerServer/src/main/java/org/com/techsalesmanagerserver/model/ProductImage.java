package org.com.techsalesmanagerserver.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String imageName;
    private String imagePath;
    @OneToOne(mappedBy = "productImage")
    private Product product;
}
