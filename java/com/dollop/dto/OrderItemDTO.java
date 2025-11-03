package com.dollop.dto;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {

	private Long orderId;
	private Long productId;
	private String productName;
	private Long quantity;
	private Long price;
}
