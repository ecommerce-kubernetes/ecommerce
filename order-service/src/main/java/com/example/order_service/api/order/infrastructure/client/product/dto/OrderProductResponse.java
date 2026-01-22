package com.example.order_service.api.order.infrastructure.client.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderProductResponse {
    private Long productId;
    private Long productVariantId;
    private ProductStatus status;
    private String sku;
    private String productName;
    private UnitPrice unitPrice;
    private Integer stockQuantity;
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
}
