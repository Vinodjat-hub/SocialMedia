package com.dollop.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.dollop.enm.Status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Long userId;
    private Double totalAmount;
    private LocalDateTime orderDate;
    private Status status;
    private List<OrderItemDTO> orderItems;  // ✅ Now DTO instead of Entity
}
