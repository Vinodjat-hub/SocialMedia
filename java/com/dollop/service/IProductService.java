package com.dollop.service;

import com.dollop.dto.ProductDTO;
import com.dollop.dto.ProductRequestDTO;
import com.dollop.entity.Product;

public interface IProductService {

	public Product saveProduct(ProductDTO productDTO);

	public Product updateProduct(ProductRequestDTO productRequestDTO);

	public Product deleteProduct(Long id);

	public Product getProductById(Long id);

	public boolean uploadProductImg(String url, Long prodId);
}
