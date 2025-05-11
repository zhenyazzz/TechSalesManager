package org.com.techsalesmanagerserver.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleDTO {
    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private double price;
} 