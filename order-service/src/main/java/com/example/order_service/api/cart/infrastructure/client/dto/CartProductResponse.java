package com.example.order_service.api.cart.infrastructure.client.dto;

import lombok.*;

import java.util.List;

@Getter
public class CartProductResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private UnitPrice unitPrice;
    private String thumbnailUrl;
    private List<ItemOption> itemOptions;

    @Builder
    private CartProductResponse(Long productId, Long productVariantId, String productName, UnitPrice unitPrice, String thumbnailUrl,
                                List<ItemOption> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.itemOptions = itemOptions;
    }

    @Getter
    @Builder
    public static class UnitPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;
    }

    @Getter
    @Builder
    public static class ItemOption {
        private String optionTypeName;
        private String optionValueName;
    }
}
