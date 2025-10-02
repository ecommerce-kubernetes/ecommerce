package com.example.product_service.repository;

import com.example.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantsRepository extends JpaRepository<ProductVariant, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuIn(Collection<String> skus);

    @Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.id = :variantId")
    Optional<ProductVariant> findWithProductById(@Param("variantId") Long variantId);

    @Query("SELECT pv FROM ProductVariant pv LEFT JOIN FETCH pv.productVariantOptions WHERE pv.product.id = :productId")
    List<ProductVariant> findWithVariantOptionByProductId(@Param("productId")Long productId);

    List<ProductVariant> findByIdIn(Collection<Long> ids);

    @Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product LEFT JOIN FETCH pv.productVariantOptions WHERE pv.id in :ids")
    List<ProductVariant> findWithProductAndOptionsByIds(@Param("ids") List<Long> ids);
}
