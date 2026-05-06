package com.example.order_service.ordersheet.domain.model.vo;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderSheetItemPriceSnapshot {
    private Money originalPrice;
    private Integer discountRate;
    private Money discountAmount;
    private Money discountedPrice;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItemPriceSnapshot(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice =discountedPrice;
    }

    public static OrderSheetItemPriceSnapshot of(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        return new OrderSheetItemPriceSnapshot(originalPrice, discountRate, discountAmount, discountedPrice);
    }
}
