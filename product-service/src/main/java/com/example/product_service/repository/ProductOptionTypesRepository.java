package com.example.product_service.repository;

import com.example.product_service.api.product.domain.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOptionTypesRepository extends JpaRepository<ProductOption, Long> {
}
