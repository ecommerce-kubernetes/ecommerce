package com.example.order_service.api.cart.infrastructure.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class CartProductResponse {
    private Long productId;
    private Long productVariantId;
    private String status;
    private String productName;
    private UnitPrice unitPrice;
    private String thumbnailUrl;
    @Builder.Default
    private List<ProductOptionInfo> productOptionInfos = new ArrayList<>();

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
    public static class ProductOptionInfo {
        private String optionTypeName;
        private String optionValueName;
    }
}
