package com.example.product_service.repository;

import com.example.product_service.entity.ProductImages;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductImagesRepository extends JpaRepository<ProductImages, Long> {
    @Query("SELECT pi FROM ProductImages pi WHERE pi.product.id =:id AND pi.sortOrder = 0")
    Optional<ProductImages> findByMainImage(@Param("id") Long id);

    @Query("SELECT pi FROM ProductImages pi WHERE pi.product.id =:productId AND pi.sortOrder =:sortOrder")
    Optional<ProductImages> findByProductIdAndSortOrder(@Param("productId") Long productId, @Param("sortOrder") int sortOrder);

    @Query("SELECT COUNT(pi) FROM ProductImages pi WHERE pi.product.id = :productId")
    int countByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("""
            UPDATE ProductImages pi 
            SET pi.sortOrder = :sortOrder
            WHERE pi.id = :imageId
            """)
    int updateSortOrder(@Param("imageId") Long imageId, @Param("sortOrder") int sortOrder);
}
