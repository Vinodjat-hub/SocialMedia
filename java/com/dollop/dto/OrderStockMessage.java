package com.dollop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStockMessage {

	private Long productId;
    private int stock;
    private Long userId;
}
