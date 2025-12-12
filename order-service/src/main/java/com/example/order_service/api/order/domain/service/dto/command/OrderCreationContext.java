package com.example.order_service.api.order.domain.service.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreationContext {
    private Long userId;
    private List<OrderItemSpec> itemSpecs;
    private CouponSpec couponSpec;
    private Long useToPoint;
    private String deliveryAddress;
    private Long finalPaymentAmount;

    @Builder
    private OrderCreationContext(Long userId, List<OrderItemSpec> itemSpecs, CouponSpec couponSpec,
                                 Long useToPoint, String deliveryAddress, Long finalPaymentAmount){
        this.userId = userId;
        this.itemSpecs = itemSpecs;
        this.couponSpec = couponSpec;
        this.useToPoint = useToPoint;
        this.deliveryAddress = deliveryAddress;
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public static OrderCreationContext of(Long userId, List<OrderItemSpec> itemSpecs, CouponSpec couponSpec, Long useToPoint,
                                          String deliveryAddress, Long finalPaymentAmount){
        return OrderCreationContext.builder()
                .userId(userId)
                .itemSpecs(itemSpecs)
                .couponSpec(couponSpec)
                .useToPoint(useToPoint)
                .deliveryAddress(deliveryAddress)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }
}
