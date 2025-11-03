package com.dollop.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String productName;
    private String productDescription;

    private MultipartFile imgFile; // ⬅️ actual uploaded file
    private String imgUrl;         // ⬅️ stored filename (optional, for update)

    private BigDecimal productPrice;
    private Integer productStock;
}

