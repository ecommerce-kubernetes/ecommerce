package com.example.product_service.service;

import com.example.product_service.dto.KafkaOrderItemDto;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ProductResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDto saveProduct(ProductRequestDto productRequestDto);
    void deleteProduct(Long productId);
    ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto);
    ProductResponseDto getProductDetails(Long productId);
    PageDto<ProductResponseDto> getProductList(Pageable pageable, Long categoryId, String name);
    List<CompactProductResponseDto> getProductListByIds(ProductRequestIdsDto productRequestIdsDto);
    void decrementStockQuantity(List<KafkaOrderItemDto> orderedItems);
    ProductResponseDto addImage(Long productId, ProductImageRequestDto productImageRequestDto);
    ProductResponseDto imgSwapOrder(Long productId, ImageOrderRequestDto imageOrderRequestDto);
    void deleteImage(Long productId, Long imageId);
}
