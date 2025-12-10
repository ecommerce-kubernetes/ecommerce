package com.example.order_service.api.order.domain.service.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderItemSpec {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private UnitPrice unitPrice;
    private int quantity;
    private Long lineTotal;
    private List<ItemOption> itemOptions;

    @Builder
    private OrderItemSpec(Long productId, Long productVariantId, String productName, String thumbnailUrl,
                          UnitPrice unitPrice, int quantity, Long lineTotal, List<ItemOption> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbnailUrl = thumbnailUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.itemOptions = itemOptions;
    }

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
