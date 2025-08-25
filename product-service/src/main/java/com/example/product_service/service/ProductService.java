package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.entity.*;
import com.example.product_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductReader productReader;

    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        return productReader.getProducts(search, pageable);
    }

    public ProductResponse getProductById(Long productId) {
        return productReader.getProductById(productId);
    }

    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId) {
        return productReader.getPopularProducts(page, size, categoryId);
    }

    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        return productReader.getReviewsByProductId(productId, pageable);
    }
}
