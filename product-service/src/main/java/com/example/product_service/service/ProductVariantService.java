package com.example.product_service.service;

import com.example.product_service.dto.request.product.CreateVariantsRequestDto;
import com.example.product_service.dto.response.product.ProductResponseDto;

public interface ProductVariantService {
    ProductResponseDto addVariants(Long productId, CreateVariantsRequestDto requestDto);
    void deleteVariant(Long variantId);
}
