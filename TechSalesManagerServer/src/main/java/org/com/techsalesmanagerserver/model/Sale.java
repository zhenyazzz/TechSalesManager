package org.com.techsalesmanagerserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sales")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int quantity;
    private BigDecimal totalPrice;
    private LocalDateTime saleDate;

    @ManyToOne
    private Product product;

    @ManyToOne
    private User user;
}
