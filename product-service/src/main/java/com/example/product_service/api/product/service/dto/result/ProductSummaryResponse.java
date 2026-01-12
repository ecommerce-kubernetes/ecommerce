package com.example.product_service.api.product.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductSummaryResponse {
    private Long productId;
    private String name;
    private String thumbnail;
    private Long displayPrice;
    private Long originalPrice;
    private Integer maxDiscountRate;
    private Long categoryId;
    private String publishedAt;
    private Double rating;
    private Long reviewCount;
    private String status;

    @Builder
    private ProductSummaryResponse(Long productId, String name, String thumbnail, Long displayPrice, Long originalPrice, Integer maxDiscountRate, Long categoryId, String publishedAt, Double rating, Long reviewCount, String status) {
        this.productId = productId;
        this.name = name;
        this.thumbnail = thumbnail;
        this.displayPrice = displayPrice;
        this.originalPrice = originalPrice;
        this.maxDiscountRate = maxDiscountRate;
        this.categoryId = categoryId;
        this.publishedAt = publishedAt;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.status = status;
    }
}
