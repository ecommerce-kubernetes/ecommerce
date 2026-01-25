package com.example.order_service.api.order.domain.service.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderCreationContext {
    private OrdererSpec orderer;
    private OrderPriceSpec orderPrice;
    private CouponSpec coupon;
    private List<OrderItemCreationContext> orderItemCreationContexts;
    private String deliveryAddress;

    public static OrderCreationContext of(OrdererSpec orderer, OrderPriceSpec orderPrice, CouponSpec coupon, List<OrderItemCreationContext> orderItemCreationContexts,
                                          String deliveryAddress) {
        return OrderCreationContext.builder()
                .orderer(orderer)
                .orderPrice(orderPrice)
                .coupon(coupon)
                .orderItemCreationContexts(orderItemCreationContexts)
                .deliveryAddress(deliveryAddress)
                .build();
    }

    @Getter
    @Builder
    public static class OrdererSpec {
        private Long userId;
        private String userName;
        private String phoneNumber;
    }

    @Getter
    @Builder
    public static class OrderPriceSpec {
        private long totalOriginPrice;
        private long totalProductDiscount;
        private long couponDiscount;
        private long pointDiscount;
        private long finalPaymentAmount;

        public static OrderPriceSpec of(long totalOriginPrice, long totalProductDiscount, long couponDiscount, long pointDiscount, long finalPaymentAmount){
            return OrderPriceSpec.builder()
                    .totalOriginPrice(totalOriginPrice)
                    .totalProductDiscount(totalProductDiscount)
                    .couponDiscount(couponDiscount)
                    .pointDiscount(pointDiscount)
                    .finalPaymentAmount(finalPaymentAmount)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CouponSpec {
        private Long couponId;
        private String couponName;
        private Long discountAmount;

        public static CouponSpec of(Long couponId, String couponName, Long discountAmount) {
            return CouponSpec.builder()
                    .couponId(couponId)
                    .couponName(couponName)
                    .discountAmount(discountAmount)
                    .build();
        }
    }
}
