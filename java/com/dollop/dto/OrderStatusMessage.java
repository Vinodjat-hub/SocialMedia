package com.dollop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusMessage {

	private Long orderId;
    private String status;
    private Long userId;

}
