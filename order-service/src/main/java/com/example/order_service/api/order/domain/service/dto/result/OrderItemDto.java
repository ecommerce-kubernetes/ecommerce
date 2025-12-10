package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItemDto {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private int quantity;
    private UnitPrice unitPrice;
    private ItemOption itemOption;
    @Builder
    @Getter
    public static class UnitPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;
    }

    @Builder
    @Getter
    public static class ItemOption {
        private String optionTypeName;
        private String optionValueName;
    }
}
