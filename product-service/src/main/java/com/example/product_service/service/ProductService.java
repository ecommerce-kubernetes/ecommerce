package com.example.product_service.service;

import com.example.product_service.dto.KafkaOrderItemDto;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.request.product.ProductBasicRequestDto;
import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.ProductSummaryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDto saveProduct(ProductRequestDto requestDto);
    PageDto<ProductSummaryDto> getProductList(Pageable pageable, Long categoryId, String name);
    ProductResponseDto getProductDetails(Long productId);
    ProductResponseDto modifyProductBasic(Long productId, ProductBasicRequestDto requestDto);
    //TODO 변경해야할 로직들
    void deleteProduct(Long productId);
    ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto);
    List<CompactProductResponseDto> getProductListByIds(ProductRequestIdsDto productRequestIdsDto);
    void decrementStockQuantity(List<KafkaOrderItemDto> orderedItems);
    ProductResponseDto addImage(Long productId, ProductImageRequestDto productImageRequestDto);
    ProductResponseDto imgSwapOrder(Long productId, ImageOrderRequestDto imageOrderRequestDto);
    void deleteImage(Long imageId);
}
