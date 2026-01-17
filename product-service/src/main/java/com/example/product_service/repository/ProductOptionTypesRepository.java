package com.example.product_service.repository;

import com.example.product_service.api.product.domain.model.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductOptionTypesRepository extends JpaRepository<ProductOption, Long> {
}
