package com.example.order_service.api.order.controller.dto.response;

import com.example.order_service.api.cart.infrastructure.client.dto.ItemOption;
import com.example.order_service.api.cart.infrastructure.client.dto.UnitPrice;
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
    private UnitPrice unitPrice;
    private int lineTotal;
    private List<ItemOption> options;

    @Builder
    private OrderItemResponse(Long productId, String productName, String thumbNailUrl, int quantity,
                              UnitPrice unitPrice, int lineTotal, List<ItemOption> options){
        this.productId = productId;
        this.productName = productName;
        this.thumbNailUrl = thumbNailUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.options = options;
    }
}
