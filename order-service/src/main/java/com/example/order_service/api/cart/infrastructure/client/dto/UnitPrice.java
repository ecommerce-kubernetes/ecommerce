package com.example.order_service.api.cart.infrastructure.client.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitPrice {
    private long originalPrice;
    private int discountRate;
    private long discountAmount;
    private long discountedPrice;

    @Builder
    private UnitPrice(long originalPrice, int discountRate, long discountAmount, long discountedPrice){
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice = discountedPrice;
    }
}
