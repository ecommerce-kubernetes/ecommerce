package com.example.product_service.service;

import com.example.product_service.dto.request.review.ReviewRequest;
import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;

public interface ProductVariantService {
    ProductVariantResponse updateVariantById(Long variantId, UpdateProductVariantRequest request);
    void deleteVariantById(Long variantId);
    ReviewResponse addReview(Long variantId, Long userId, ReviewRequest request);
}
