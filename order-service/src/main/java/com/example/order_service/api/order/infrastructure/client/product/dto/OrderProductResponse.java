package com.example.order_service.api.order.infrastructure.client.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class OrderProductResponse {
    private Long productId;
    private Long productVariantId;
    private String status;
    private String sku;
    private String productName;
    private String thumbnailUrl;
    private UnitPrice unitPrice;
    private Integer stockQuantity;
    @Builder.Default
    private List<ProductOptionInfo> itemOptions = new ArrayList<>();

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
