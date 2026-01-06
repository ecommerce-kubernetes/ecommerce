package com.example.order_service.api.order.infrastructure.client.product.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProductResponse {
    private Long productId;
    private Long productVariantId;
    private String sku;
    private String productName;
    private UnitPrice unitPrice;
    private Integer stockQuantity;
    private String thumbnailUrl;
    private List<ItemOption> itemOptions;

    @Builder
    private OrderProductResponse(Long productId, Long productVariantId, String sku, String productName, UnitPrice unitPrice, Integer stockQuantity,
                                 String thumbnailUrl, List<ItemOption> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.sku = sku;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
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
