package com.dollop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dollop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {	
	
	@Query("""
	        SELECT p FROM Product p
	        WHERE (:name IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%')))
	          AND (:minPrice IS NULL OR p.productPrice >= :minPrice)
	          AND (:maxPrice IS NULL OR p.productPrice <= :maxPrice)
	        ORDER BY
	          CASE WHEN :sort = 'highest' THEN p.productPrice END DESC,
	          CASE WHEN :sort = 'lowest' THEN p.productPrice END ASC
	    """)
	    Page<Product> findAllWithFilters(
	        @Param("name") String name,
	        @Param("minPrice") Double minPrice,
	        @Param("maxPrice") Double maxPrice,
	        @Param("sort") String sort,
	        Pageable pageable
	    );
	
	@Query("""
	        SELECT p FROM Product p
	        WHERE (:name IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :name, '%')))
	          AND (:minPrice IS NULL OR p.productPrice >= :minPrice)
	          AND (:maxPrice IS NULL OR p.productPrice <= :maxPrice)
	          AND TRIM(LOWER(p.status)) = 'active'
	        ORDER BY
	          CASE WHEN :sort = 'highest' THEN p.productPrice END DESC,
	          CASE WHEN :sort = 'lowest' THEN p.productPrice END ASC
	    """)
	Page<Product> findAllActiveWithFilters(String name, Double minPrice, Double maxPrice, String sort,
			Pageable pageable);

}
