package com.example.product_service.service;

import com.example.product_service.dto.request.ProductRequestDto;
import com.example.product_service.dto.request.StockQuantityRequestDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDto saveProduct(ProductRequestDto productRequestDto);
    void deleteProduct(Long productId);
    ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto);
    ProductResponseDto getProductDetails(Long productId);
    PageDto<ProductResponseDto> getProductList(Pageable pageable, Long categoryId, String name);
}
