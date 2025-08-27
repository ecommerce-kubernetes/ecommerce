package com.example.product_service.repository;

import com.example.product_service.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductsRepository extends JpaRepository<Products, Long>{

    @Query("SELECT p FROM Products p LEFT JOIN FETCH p.category WHERE p.id = :productId")
    Optional<Products> findWithCategoryById(@Param("productId") Long productId);

}
