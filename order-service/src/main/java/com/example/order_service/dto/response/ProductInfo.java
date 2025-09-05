package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProductInfo {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private int price;
    private int discountRate;
    private String thumbnailUrl;
    private List<ItemOptionResponse> itemOptions;

    public long calcDiscountPrice(){
        return Math.round(price * (1 - discountRate / 100.0));
    }
}
