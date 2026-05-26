package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductPriceSnapshot {
    private Money originalPrice;
    private Integer discountRate;
    private Money discountAmount;
    private Money discountedPrice;

    @Builder(builderMethodName = "reconstitute")
    private ProductPriceSnapshot(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice =discountedPrice;
    }

    public static ProductPriceSnapshot of(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        return new ProductPriceSnapshot(originalPrice, discountRate, discountAmount, discountedPrice);
    }
}
