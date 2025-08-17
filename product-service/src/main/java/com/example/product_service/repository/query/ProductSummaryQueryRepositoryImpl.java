package com.example.product_service.repository.query;

import com.example.product_service.entity.ProductSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductSummaryQueryRepositoryImpl implements ProductSummaryQueryRepository{
    @Override
    public Page<ProductSummary> findAllProductSummary(String name, List<Long> categoryIds, Integer rating, Pageable pageable) {
        return null;
    }
}
