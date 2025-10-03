package com.example.product_service.repository;

import com.example.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Product, Long>{

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :productId")
    Optional<Product> findWithCategoryById(@Param("productId") Long productId);

}
