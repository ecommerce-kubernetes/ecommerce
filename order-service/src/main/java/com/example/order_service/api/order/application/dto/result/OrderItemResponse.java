package com.example.order_service.api.order.application.dto.result;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbNailUrl;
    private int quantity;
    private OrderItemPrice unitPrice;
    private Long lineTotal;
    private List<OrderItemOption> options;

    @Builder
    private OrderItemResponse(Long productId, Long productVariantId, String productName, String thumbNailUrl, int quantity,
                              OrderItemPrice unitPrice, Long lineTotal, List<OrderItemOption> options){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.thumbNailUrl = thumbNailUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.options = options;
    }

    @Getter
    @Builder
    public static class OrderItemPrice {
        private long originalPrice;
        private int discountRate;
        private long discountAmount;
        private long discountedPrice;
    }

    @Getter
    @Builder
    public static class OrderItemOption {
        private String optionTypeName;
        private String optionValueName;
    }
}
