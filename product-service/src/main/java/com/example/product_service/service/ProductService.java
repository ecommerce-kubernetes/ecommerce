package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto productRequestDto);
}
