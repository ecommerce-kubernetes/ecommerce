package com.example.order_service.order.application.dto.result;

import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {
    private String orderNo;
    private String status;
    private String orderName;
    private OrdererResponse orderer;
    private OrderPriceResponse orderPrice;
    private CouponResponse coupon;
    private String deliveryAddress;
    private PaymentResponse payment;
    private List<OrderItemResponse> orderItems;
    private String createdAt;

    public static OrderDetailResponse from(OrderDto orderDto) {
        return null;
    }

    @Getter
    @Builder
    public static class OrderPriceResponse {
        private Long totalOriginPrice;
        private Long totalProductDiscount;
        private Long couponDiscount;
        private Long pointDiscount;
        private Long finalPaymentAmount;
    }
    @Getter
    @Builder
    public static class CouponResponse {
        private Long couponId;
        private String couponName;
        private Long couponDiscount;
    }
    @Getter
    @Builder
    public static class OrdererResponse {
        private Long userId;
        private String userName;
        private String phoneNumber;

    }

    @Getter
    @Builder
    public static class PaymentResponse {
        private Long paymentId;
        private String paymentKey;
        private Long amount;
        private String status;
        private String method;
        private String approvedAt;

    }
}
