package com.example.product_service.repository;

import com.example.product_service.entity.ProductVariants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ProductVariantsRepository extends JpaRepository<ProductVariants, Long> {

    boolean existsBySku(String sku);
    boolean existsBySkuIn(Collection<String> skus);
}
