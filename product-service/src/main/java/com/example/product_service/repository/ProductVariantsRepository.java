package com.example.product_service.repository;

import com.example.product_service.entity.ProductVariants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantsRepository extends JpaRepository<ProductVariants, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuIn(Collection<String> skus);

    @Query("SELECT pv FROM ProductVariants pv JOIN FETCH pv.product WHERE pv.id = :variantId")
    Optional<ProductVariants> findWithProductById(@Param("variantId") Long variantId);

    @Query("SELECT pv FROM ProductVariants pv LEFT JOIN FETCH pv.productVariantOptions WHERE pv.product.id = :productId")
    List<ProductVariants> findWithVariantOptionByProductId(@Param("productId")Long productId);
}
