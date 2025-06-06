package com.example.product_service.service;

import com.example.product_service.dto.request.ImageOrderRequestDto;
import com.example.product_service.dto.response.product.ProductResponseDto;

public interface ProductImageService {
    void deleteImage(Long imageId);
    ProductResponseDto imgSwapOrder(Long imageId, ImageOrderRequestDto requestDto);
}
