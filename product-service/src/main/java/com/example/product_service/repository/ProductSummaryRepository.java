package com.example.product_service.repository;

import com.example.product_service.entity.ProductSummary;
import com.example.product_service.repository.query.ProductSummaryQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSummaryRepository extends JpaRepository<ProductSummary, Long>, ProductSummaryQueryRepository {
}
