package com.example.order_service.api.order.infrastructure.client.dto;

import com.example.order_service.api.cart.infrastructure.client.dto.ItemOption;
import com.example.order_service.api.cart.infrastructure.client.dto.UnitPrice;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderProductResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private UnitPrice unitPrice;
    private String thumbnailUrl;
    private List<ItemOption> itemOptions;

    @Builder
    private OrderProductResponse(Long productId, Long productVariantId, String productName, UnitPrice unitPrice, String thumbnailUrl,
                                List<ItemOption> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.itemOptions = itemOptions;
    }
}
