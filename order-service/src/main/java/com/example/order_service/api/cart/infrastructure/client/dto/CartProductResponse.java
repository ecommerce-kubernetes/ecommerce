package com.example.order_service.api.cart.infrastructure.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CartProductResponse {
    private Long productId;
    private Long productVariantId;
    private ProductStatus status;
    private String productName;
    private UnitPrice unitPrice;
    private String thumbnailUrl;
    private List<ItemOption> itemOptions;

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

    public boolean isOnSale() {
        return this.status == ProductStatus.ON_SALE;
    }
}
