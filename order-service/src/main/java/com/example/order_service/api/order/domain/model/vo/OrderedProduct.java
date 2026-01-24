package com.example.order_service.api.order.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderedProduct {
    private Long productId;
    private Long productVariantId;
    private String sku;
    private String productName;
    private String thumbnail;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderedProduct(Long productId, Long productVariantId, String sku, String productName, String thumbnail) {
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.sku = sku;
        this.productName = productName;
        this.thumbnail = thumbnail;
    }

    public static OrderedProduct of(Long productId, Long productVariantId, String sku, String productName, String thumbnail) {
        return OrderedProduct.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .sku(sku)
                .productName(productName)
                .thumbnail(thumbnail)
                .build();
    }
}
