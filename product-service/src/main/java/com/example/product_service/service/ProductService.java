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
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponse saveProduct(ProductRequest request);
    List<ImageResponse> addImages(Long productId, AddImageRequest request);
    ProductVariantResponse addVariant(Long productId, ProductVariantRequest request);
    PageDto<ProductSummaryResponse> getProducts(ProductSearch search, Pageable pageable);
    ProductResponse getProductById(Long productId);
    PageDto<ReviewResponse> getReviewsByProductId(Long productId, Pageable pageable);
    PageDto<ProductSummaryResponse> getPopularProducts(int page, int size, Long categoryId);
    ProductUpdateResponse updateBasicInfoById(Long productId, UpdateProductBasicRequest request);
    void deleteProductById(Long productId);
}
