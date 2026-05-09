package com.example.order_service.ordersheet.domain.model.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItemProductSnapshot {
    private Long productId;
    private Long productVariantId;
    private String sku;
    private String productName;
    private String thumbnail;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItemProductSnapshot(Long productId, Long productVariantId, String sku, String productName, String thumbnail) {
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.sku = sku;
        this.productName = productName;
        this.thumbnail = thumbnail;
    }

    public static OrderSheetItemProductSnapshot of(Long productId, Long productVariantId, String sku, String productName, String thumbnail) {
        return new OrderSheetItemProductSnapshot(productId, productVariantId, sku, productName, thumbnail);
    }
}
