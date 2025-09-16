package com.example.order_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SuccessOrderItemDto {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String optionJson;
    private long originPrice;
    private long discountRate;
    private long discountedPrice;
    private long finalPrice;
    private int quantity;
    private String thumbnail;
}
