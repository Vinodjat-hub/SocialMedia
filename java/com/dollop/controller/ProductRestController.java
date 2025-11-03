package com.dollop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dollop.dto.ProductDTO;
import com.dollop.dto.ProductRequestDTO;
import com.dollop.entity.Product;
import com.dollop.service.impl.ProductServiceImpl;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/api/products")
public class ProductRestController {

	@Autowired
	private ProductServiceImpl productServiceImpl;

	@PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Product> addProduct(@ModelAttribute ProductDTO productDTO) {
		Product saved = productServiceImpl.saveProduct(productDTO);
		System.err.println(saved);
		return ResponseEntity.ok(saved);
	}

	@PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Product> updateProduct(@ModelAttribute ProductRequestDTO product) {
		return new ResponseEntity<>(productServiceImpl.updateProduct(product), HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
		return new ResponseEntity<Product>(productServiceImpl.deleteProduct(id), HttpStatus.OK);
	}

	@PutMapping("/active/{id}")
	public ResponseEntity<Product> activeProduct(@PathVariable Long id) {
		return new ResponseEntity<Product>(productServiceImpl.activeProduct(id), HttpStatus.OK);
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<Product> getProductById(@PathVariable Long id) {
		return new ResponseEntity<Product>(productServiceImpl.getProductById(id), HttpStatus.OK);
	}

	@PutMapping("/updateImg/{prodId}")
	public ResponseEntity<Boolean> updateProductImg(@PathVariable Long prodId, @RequestParam String url) {
		return new ResponseEntity<>(productServiceImpl.uploadProductImg(url, prodId), HttpStatus.OK);
	}

	@GetMapping("/getAllProducts")
	public ResponseEntity<Page<Product>> getAllProducts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size, @RequestParam(required = false) String name,
			@RequestParam(required = false) String category, @RequestParam(required = false) Double minPrice,
			@RequestParam(required = false) Double maxPrice, @RequestParam(required = false) String sort) {

		Page<Product> products = productServiceImpl.getProducts(page, size, name, minPrice, maxPrice, sort);
		return ResponseEntity.ok(products);
	}
	
	@GetMapping("/getAllActiveProducts")
	public ResponseEntity<Page<Product>> getAllActiveProducts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size, @RequestParam(required = false) String name,
			@RequestParam(required = false) String category, @RequestParam(required = false) Double minPrice,
			@RequestParam(required = false) Double maxPrice, @RequestParam(required = false) String sort) {

		Page<Product> products = productServiceImpl.getActiveProducts(page, size, name, minPrice, maxPrice, sort);
		return ResponseEntity.ok(products);
	}
}
