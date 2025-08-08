package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse saveProduct(ProductRequest request);
    PageDto<ProductSummaryResponse> getProducts(Pageable pageable, ProductSearch search);
    ProductResponse getProductById(Long productId);
    PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId);
    ProductUpdateResponse updateBasicInfo(Long productId, UpdateProductBasicRequest request);
}
