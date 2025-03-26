package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.ProductResponseDto;

public interface ProductService {
    ProductResponseDto saveProduct(ProductRequestDto productRequestDto);
    void deleteProduct(Long productId);
    ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto);
    ProductResponseDto getProductDetails(Long productId);
}
