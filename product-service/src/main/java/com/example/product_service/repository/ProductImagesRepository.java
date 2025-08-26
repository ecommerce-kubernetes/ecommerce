package com.example.product_service.repository;

import com.example.product_service.entity.ProductImages;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImagesRepository extends JpaRepository<ProductImages, Long> {
    @Query("SELECT i FROM ProductImages i WHERE i.product.id = :productId")
    List<ProductImages> findByProductId(@Param("productId") Long productId);

    @Query("SELECT i FROM ProductImages i JOIN FETCH i.product WHERE i.id = :imageId")
    Optional<ProductImages> findWithProductById(@Param("imageId") Long imageId);
}
