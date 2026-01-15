package com.example.product_service.repository;

import com.example.product_service.api.product.domain.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImagesRepository extends JpaRepository<ProductImage, Long> {
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId")
    List<ProductImage> findByProductId(@Param("productId") Long productId);

    @Query("SELECT i FROM ProductImage i JOIN FETCH i.product WHERE i.id = :imageId")
    Optional<ProductImage> findWithProductById(@Param("imageId") Long imageId);
}
