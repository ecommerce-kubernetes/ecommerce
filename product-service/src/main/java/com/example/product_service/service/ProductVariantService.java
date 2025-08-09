package com.example.product_service.service;

import com.example.product_service.dto.request.variant.UpdateProductVariantRequest;
import com.example.product_service.dto.response.variant.ProductVariantResponse;

public interface ProductVariantService {
    ProductVariantResponse updateVariantById(Long variantId, UpdateProductVariantRequest request);
}
