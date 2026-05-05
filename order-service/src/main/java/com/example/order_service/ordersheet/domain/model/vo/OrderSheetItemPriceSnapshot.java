package com.example.order_service.ordersheet.domain.model.vo;

import lombok.Getter;

@Getter
public class OrderSheetItemPriceSnapshot {
    private Long originalPrice;
    private Integer discountRate;
    private Long discountAmount;
    private Long discountedPrice;

    private OrderSheetItemPriceSnapshot(Long originalPrice, Integer discountRate, Long discountAmount, Long discountedPrice) {
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice =discountedPrice;
    }

    public static OrderSheetItemPriceSnapshot of(Long originalPrice, Integer discountRate, Long discountAmount, Long discountedPrice) {
        return new OrderSheetItemPriceSnapshot(originalPrice, discountRate, discountAmount, discountedPrice);
    }
}
