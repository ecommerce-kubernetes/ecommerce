package com.example.order_service.order.domain.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductSnapshot {
    private Long productId;
    private Long productVariantId;
    private String sku;
    private String productName;
    private String thumbnail;

    @Builder(builderMethodName = "reconstitute")
    private ProductSnapshot(Long productId, Long productVariantId, String sku, String productName, String thumbnail) {
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.sku = sku;
        this.productName = productName;
        this.thumbnail = thumbnail;
    }

    public static ProductSnapshot of(Long productId, Long productVariantId, String sku, String productName, String thumbnail) {
        return new ProductSnapshot(productId, productVariantId, sku, productName, thumbnail);
    }
}
