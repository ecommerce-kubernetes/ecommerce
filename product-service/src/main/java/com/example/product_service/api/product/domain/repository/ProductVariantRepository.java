package com.example.product_service.api.product.domain.repository;

import com.example.product_service.api.product.domain.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}
