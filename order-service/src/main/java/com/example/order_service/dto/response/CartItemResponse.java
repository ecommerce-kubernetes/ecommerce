package com.example.order_service.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String thumbNailUrl;
    private int quantity;
    private UnitPrice unitPrice;
    private long lineTotal;
    private List<ItemOptionResponse> options;
    private boolean isAvailable;

    @Builder
    private CartItemResponse(Long id, Long productId, String productName, String thumbNailUrl, int quantity,
                             UnitPrice unitPrice, long lineTotal, List<ItemOptionResponse> options, boolean isAvailable){
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.thumbNailUrl = thumbNailUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.options = options;
        this.isAvailable = isAvailable;
    }
}
