package com.example.order_service.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String thumbNailUrl;
    private int quantity;
    private UnitPriceInfo unitPriceInfo;
    private int lineTotal;
    private List<ItemOptionResponse> options;

    @Builder
    private OrderItemResponse(Long productId, String productName, String thumbNailUrl, int quantity,
                              UnitPriceInfo unitPriceInfo, int lineTotal, List<ItemOptionResponse> options){
        this.productId = productId;
        this.productName = productName;
        this.thumbNailUrl = thumbNailUrl;
        this.quantity = quantity;
        this.unitPriceInfo = unitPriceInfo;
        this.lineTotal = lineTotal;
        this.options = options;
    }
}
