package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductQueryService productQueryService;

    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        return productQueryService.getProducts(search, pageable);
    }

    public ProductResponse getProductById(Long productId) {
        return productQueryService.getProductById(productId);
    }

    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId) {
        return productQueryService.getPopularProducts(page, size, categoryId);
    }

    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        return productQueryService.getReviewsByProductId(productId, pageable);
    }
}
