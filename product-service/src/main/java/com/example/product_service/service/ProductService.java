package com.example.product_service.service;

import com.example.product_service.dto.KafkaOrderItemDto;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.request.options.IdsRequestDto;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.product.ProductRequestDto;
import com.example.product_service.dto.response.CompactProductResponseDto;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.product.ProductResponseDto;
import com.example.product_service.dto.response.product.ProductSummaryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDto saveProduct(ProductRequestDto requestDto);
    PageDto<ProductSummaryDto> getProductList(Pageable pageable, Long categoryId, String name, Integer rating);
    ProductResponseDto getProductDetails(Long productId);
    ProductResponseDto modifyProductBasic(Long productId, UpdateProductBasicRequest requestDto);
    void deleteProduct(Long productId);
    void batchDeleteProducts(IdsRequestDto requestDto);
    PageDto<ProductSummaryDto> getPopularProductList(Pageable pageable, Long categoryId);
    PageDto<ProductSummaryDto> getSpecialSale(Pageable pageable, Long categoryId);
    //TODO 변경해야할 로직들
    ProductResponseDto modifyStockQuantity(Long productId, StockQuantityRequestDto stockQuantityRequestDto);
    List<CompactProductResponseDto> getProductListByIds(ProductRequestIdsDto productRequestIdsDto);
    void decrementStockQuantity(List<KafkaOrderItemDto> orderedItems);
}
