package com.example.order_service.order.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
        Assert.notNull(productId, "상품 식별자는 필수 입니다.");
        Assert.notNull(productVariantId, "상품 변형 식별자는 필수 입니다.");
        Assert.hasText(sku, "상품 SKU는 필수 입니다.");
        Assert.hasText(productName, "상품명은 필수 입니다.");
        Assert.hasText(thumbnail, "상품 썸네일은 필수 입니다.");

        return new ProductSnapshot(productId, productVariantId, sku, productName, thumbnail);
    }
}
