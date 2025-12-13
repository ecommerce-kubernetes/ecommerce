package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderItemDto {
    private Long orderItemId;
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnailUrl;
    private int quantity;
    private Long lineTotal;
    private UnitPrice unitPrice;
    private List<ItemOption> itemOptions;

    @Builder
    private OrderItemDto(Long orderItemId, Long productId, Long productVariantId, String productName, String thumbnailUrl, int quantity,
                         Long lineTotal, UnitPrice unitPrice, List<ItemOption> itemOptions){
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbnailUrl = thumbnailUrl;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.unitPrice = unitPrice;
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
