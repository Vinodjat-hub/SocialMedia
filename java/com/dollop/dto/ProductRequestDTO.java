package com.dollop.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

	private Long productId;
	private String productName;
	private String productDescription;
	private MultipartFile imgUrl;
	private Double productPrice; // ✅ Better for currency
	private Integer productStock;
}
