package com.example.product_service.repository.query;

import com.example.product_service.entity.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSummaryQueryRepository {
    Page<ProductSummary> findAllProductSummary(String name, List<Long> categoryIds, Integer rating, Pageable pageable);
}
