package com.example.product_service.api.product.service.dto;

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
    private String createdAt;
    private Double rating;
    private Long reviewCount;
    private String status;

}
