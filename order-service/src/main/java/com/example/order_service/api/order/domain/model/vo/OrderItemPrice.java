package com.example.order_service.api.order.domain.model.vo;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemPrice {
    private Long originPrice;
    private Integer discountRate;
    private Long discountAmount;
    private Long discountedPrice;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemPrice(Long originPrice, Integer discountRate, Long discountAmount, Long discountedPrice) {
        this.originPrice = originPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice = discountedPrice;
    }

    public static OrderItemPrice of(Long originPrice, Integer discountRate, Long discountAmount, Long discountedPrice) {
        return OrderItemPrice.builder()
                .originPrice(originPrice)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .discountedPrice(discountedPrice)
                .build();
    }
}
