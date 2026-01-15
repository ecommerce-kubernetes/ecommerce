package com.example.product_service.api.product.domain.repository;

import com.example.product_service.api.product.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>{

    @Query("select count(p) > 0 from Product p where p.category.id = :categoryId")
    boolean existsByCategoryId(@Param("categoryId") Long categoryId);

}
