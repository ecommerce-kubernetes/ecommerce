package com.example.order_service.order.application.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderItemResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private int quantity;
    private OrderItemPriceResponse unitPrice;
    private Long lineTotal;
    private List<OrderItemOptionResponse> options;

    @Getter
    @Builder
    public static class OrderItemPriceResponse {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;

    }
    @Getter
    @Builder
    public static class OrderItemOptionResponse {
        private String optionTypeName;
        private String optionValueName;

    }
}
