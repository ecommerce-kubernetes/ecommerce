package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService{
    @Override
    public ProductUpdateResponse updateBasicInfo(Long productId, UpdateProductBasicRequest request) {
        return null;
    }

    @Override
    public void deleteProduct(Long productId) {

    }

    @Override
    public PageDto<ProductSummaryResponse> getProducts(Pageable pageable, ProductSearch search) {
        return null;
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        return null;
    }

    @Override
    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId) {
        return null;
    }

    @Override
    public ProductResponse saveProduct(ProductRequest request) {
        return new ProductResponse();
    }
}
