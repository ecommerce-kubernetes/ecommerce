package com.example.product_service.product.application.service;

import com.example.product_service.product.application.service.dto.command.SearchProductCommand;
import com.example.product_service.product.application.service.dto.result.ProductDetailResult;
import com.example.product_service.product.application.service.dto.result.ProductSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    public Page<ProductSummaryResult> getProducts(SearchProductCommand command) {
        return null;
    }

    public ProductDetailResult getProduct(Long productId) {
        return null;
    }
}
