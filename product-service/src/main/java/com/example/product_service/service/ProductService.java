package com.example.product_service.service;

import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.*;
import com.example.product_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductSaver productSaver;
    private final ProductReader productReader;

    public ProductResponse saveProduct(ProductRequest request) {
        return productSaver.saveProduct(request);
    }

    public PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable) {
        return productReader.getProducts(search, pageable);
    }

    public ProductResponse getProductById(Long productId) {
        return productReader.getProductById(productId);
    }

    public PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable) {
        return null;
    }

    public PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId) {
        return null;
    }

    public ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request) {
        return null;
    }

    public void deleteProductById(Long productId) {

    }
    public List<ImageResponse> addImages(Long productId, AddImageRequest request) {
        return List.of();
    }


    public ProductVariantResponse addVariant(Long productId, ProductVariantRequest request) {
        return null;
    }

}
