package com.example.order_service.api.cart.infrastructure.client.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartProductResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private UnitPrice unitPrice;
    private String thumbnailUrl;
    private List<ItemOption> itemOptions;

    @Builder
    private CartProductResponse(Long productId, Long productVariantId, String productName, UnitPrice unitPrice, String thumbnailUrl,
                                List<ItemOption> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.itemOptions = itemOptions;
    }
}
