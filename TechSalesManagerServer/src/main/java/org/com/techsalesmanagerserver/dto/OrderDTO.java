package org.com.techsalesmanagerserver.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderDTO {
    private Long id;
    private Long userId;
    private String customerName;
    private LocalDateTime orderDate;
    private String status;
    private double totalAmount;
} 