package com.example.order_service.api.order.application.dto.result;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private String orderName;
    private String deliveryAddress;
    private PaymentInfo paymentInfo;
    private CouponInfo couponInfo;
    private List<OrderItemResponse> orderItems;

    @Builder
    private OrderResponse(Long orderId, Long userId, String orderStatus, String orderName, String deliveryAddress, PaymentInfo paymentInfo, CouponInfo couponInfo, List<OrderItemResponse> orderItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.paymentInfo = paymentInfo;
        this.couponInfo = couponInfo;
        this.orderItems = orderItems;
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentInfo {
        private Long totalOriginPrice;
        private Long totalProductDiscount;
        private Long couponDiscount;
        private Long pointDiscount;
        private Long finalPaymentAmount;

        @Builder
        private PaymentInfo(Long totalOriginPrice, Long totalProductDiscount, Long couponDiscount, Long pointDiscount, Long finalPaymentAmount) {
            this.totalOriginPrice = totalOriginPrice;
            this.totalProductDiscount = totalProductDiscount;
            this.couponDiscount = couponDiscount;
            this.pointDiscount = pointDiscount;
            this.finalPaymentAmount = finalPaymentAmount;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CouponInfo {
        private Long couponId;
        private String couponName;
        private Long couponDiscount;

        @Builder
        private CouponInfo(Long couponId, String couponName, Long couponDiscount) {
            this.couponId = couponId;
            this.couponName = couponName;
            this.couponDiscount = couponDiscount;
        }
    }
}
