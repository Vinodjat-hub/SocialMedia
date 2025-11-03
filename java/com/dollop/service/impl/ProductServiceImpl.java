package com.dollop.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dollop.dto.ProductDTO;
import com.dollop.dto.ProductRequestDTO;
import com.dollop.entity.Product;
import com.dollop.exception.ResourceNotFoundException;
import com.dollop.repository.ProductRepository;
import com.dollop.service.IProductService;

import io.jsonwebtoken.io.IOException;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	private ProductRepository productRepository;

	@Override
	public Product saveProduct(ProductDTO productDTO) {
		Product product = new Product();
		BeanUtils.copyProperties(productDTO, product, "imgFile");

		MultipartFile file = productDTO.getImgFile();
		if (file != null && !file.isEmpty()) {
			String contentType = file.getContentType();
			if (contentType != null && contentType.startsWith("image/")) {
				String originalFilename = file.getOriginalFilename();
				String extension = originalFilename != null
						? originalFilename.substring(originalFilename.lastIndexOf("."))
						: "";

				String fileName = UUID.randomUUID() + "_" + originalFilename;

				Path uploadDir = Paths.get("uploads");
				if (!Files.exists(uploadDir)) {
					try {
						Files.createDirectories(uploadDir);
					} catch (java.io.IOException e) {
						e.printStackTrace();
					}
				}
				Path path = uploadDir.resolve(fileName);
				try {
					Files.write(path, file.getBytes());
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}

				product.setImgUrl(fileName);
			} else {
				throw new ResourceNotFoundException("Only image files (.jpg, .jpeg, .png, .webp, etc.) are allowed!");
			}
		}

		product.setStatus("ACTIVE");
		return productRepository.save(product);
	}

	@Override
	public Product updateProduct(ProductRequestDTO productDTO) {
		Product existing = productRepository.findById(productDTO.getProductId()).orElseThrow(
				() -> new ResourceNotFoundException("Product not found with id: " + productDTO.getProductId()));

		BeanUtils.copyProperties(productDTO, existing, "id", "imgFile", "imgUrl");

		MultipartFile file = productDTO.getImgUrl();
		if (file != null && !file.isEmpty()) {
			String contentType = file.getContentType();
			if (contentType != null && contentType.startsWith("image/")) {
				String originalFilename = file.getOriginalFilename();
				String extension = originalFilename != null && originalFilename.contains(".")
						? originalFilename.substring(originalFilename.lastIndexOf("."))
						: "";

				String fileName = UUID.randomUUID() + extension;

				try {
					Path uploadDir = Paths.get("uploads");
					if (!Files.exists(uploadDir)) {
						Files.createDirectories(uploadDir);
					}
					Path path = uploadDir.resolve(fileName);
					Files.write(path, file.getBytes());

					existing.setImgUrl(fileName);
				} catch (IOException | java.io.IOException e) {
					throw new ResourceNotFoundException("File upload failed", e);
				}
			} else {
				throw new ResourceNotFoundException("Only image files (.jpg, .jpeg, .png, .webp, etc.) are allowed!");
			}
		}

		return productRepository.save(existing);
	}

	@Override
	public Product deleteProduct(Long id) {
		Optional<Product> prod = productRepository.findById(id);
		if (prod.isEmpty())
			throw new ResourceNotFoundException("Product not available on this product_id : " + id);
		try {
			prod.get().setStatus("INACTIVE");
			productRepository.save(prod.get());
		} catch (Exception e) {
			throw new ResourceNotFoundException("Failed to delete product because it is ordered!!");
		}
		return prod.get();
	}

	public Product activeProduct(Long id) {
		Optional<Product> prod = productRepository.findById(id);
		if (prod.isEmpty())
			throw new ResourceNotFoundException("Product not available on this product_id : " + id);
		try {
			prod.get().setStatus("ACTIVE");
			productRepository.save(prod.get());
		} catch (Exception e) {
			throw new ResourceNotFoundException("Failed to delete product because it is ordered!!");
		}
		return prod.get();
	}

	@Override
	public Product getProductById(Long id) {
		Optional<Product> prod = productRepository.findById(id);
		if (prod.isEmpty())
			throw new ResourceNotFoundException("Product not available on this product_id : " + id);
		return prod.get();
	}

	@Override
	public boolean uploadProductImg(String url, Long prodId) {
		Optional<Product> prod = productRepository.findById(prodId);
		if (prod.isEmpty())
			throw new ResourceNotFoundException("Product not available on this product_id : " + prodId);
		Product product = new Product();
		BeanUtils.copyProperties(prod, product);
		product.setImgUrl(url);
		product = productRepository.save(product);
		if (product == null) {
			throw new RuntimeException("Image not uploaded!!!");
		}
		return true;
	}

//	public Page<Product> getProducts(int page, int size, String name, Double minPrice, Double maxPrice, String sort) {
//		Pageable pageable = (Pageable) PageRequest.of(page, size);
//		if(name != null && minPrice != null && maxPrice != null) {
//			return productRepository.findAllWithFilters(name, minPrice, maxPrice, pageable);
//		}
//		if(name != null && minPrice != null ) {
//			return productRepository.findAllByProductNameContainingIgnoreCaseAndProductPriceGreaterThanEqual(name, minPrice, pageable);
//		}
//		if(name != null && maxPrice != null) {
//			return productRepository.findAllByProductNameContainingIgnoreCaseAndProductPriceLessThanEqual(name, maxPrice, pageable);
//		}
//		if (name != null) {
//			return productRepository.findByProductNameContainingIgnoreCase(name, pageable);
//		}
//
//		if (minPrice != null || maxPrice != null) {
//			return productRepository.findAllWithFilters(name, minPrice, maxPrice, pageable);
//		}
//
//		if (sort != null && sort.equals("highest")) {
//			return productRepository.findAllByHighestPrices(pageable);
//		}
//
//		if (sort != null && sort.equals("lowest")) {
//			return productRepository.findAllByLowestPrices(pageable);
//		}
//
//		return productRepository.findAll(pageable);
//	}

	public Page<Product> getProducts(int page, int size, String name, Double minPrice, Double maxPrice, String sort) {
		Pageable pageable = PageRequest.of(page, size);
		return productRepository.findAllWithFilters(name, minPrice, maxPrice, sort, pageable);
	}

	public Page<Product> getActiveProducts(int page, int size, String name, Double minPrice, Double maxPrice, String sort) {
		Pageable pageable = PageRequest.of(page, size);
		return productRepository.findAllActiveWithFilters(name, minPrice, maxPrice, sort, pageable);
	}
}
