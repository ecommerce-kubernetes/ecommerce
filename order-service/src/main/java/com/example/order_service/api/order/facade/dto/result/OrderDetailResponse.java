package com.example.order_service.api.order.facade.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.CouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.OrderPriceInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.OrdererInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto.PaymentInfo;
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
        List<OrderItemResponse> orderItems = orderDto.getOrderItems().stream().map(OrderItemResponse::from).toList();
        return OrderDetailResponse.builder()
                .orderNo(orderDto.getOrderNo())
                .status(orderDto.getStatus().toString())
                .orderName(orderDto.getOrderName())
                .orderer(OrdererResponse.from(orderDto.getOrderer()))
                .orderPrice(OrderPriceResponse.from(orderDto.getOrderPriceInfo()))
                .coupon(CouponResponse.from(orderDto.getCouponInfo()))
                .deliveryAddress(orderDto.getDeliveryAddress())
                .payment(PaymentResponse.from(orderDto.getPaymentInfo()))
                .orderItems(orderItems)
                .createdAt(orderDto.getOrderedAt().toString())
                .build();
    }

    @Getter
    @Builder
    public static class OrderPriceResponse {
        private Long totalOriginPrice;
        private Long totalProductDiscount;
        private Long couponDiscount;
        private Long pointDiscount;
        private Long finalPaymentAmount;

        private static OrderPriceResponse from(OrderPriceInfo orderPriceInfo) {
            return OrderPriceResponse.builder()
                    .totalOriginPrice(orderPriceInfo.getTotalOriginPrice())
                    .totalProductDiscount(orderPriceInfo.getTotalProductDiscount())
                    .couponDiscount(orderPriceInfo.getCouponDiscount())
                    .pointDiscount(orderPriceInfo.getPointDiscount())
                    .finalPaymentAmount(orderPriceInfo.getFinalPaymentAmount())
                    .build();
        }

    }
    @Getter
    @Builder
    public static class CouponResponse {
        private Long couponId;
        private String couponName;
        private Long couponDiscount;

        private static CouponResponse from(CouponInfo couponInfo) {
            if (couponInfo == null) {
                return null;
            }
            return CouponResponse.builder()
                    .couponId(couponInfo.getCouponId())
                    .couponName(couponInfo.getCouponName())
                    .couponDiscount(couponInfo.getDiscountAmount())
                    .build();
        }

    }
    @Getter
    @Builder
    public static class OrdererResponse {
        private Long userId;
        private String userName;
        private String phoneNumber;

        private static OrdererResponse from(OrdererInfo ordererInfo) {
            return OrdererResponse.builder()
                    .userId(ordererInfo.getUserId())
                    .userName(ordererInfo.getUserName())
                    .phoneNumber(ordererInfo.getPhoneNumber())
                    .build();
        }
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

        private static PaymentResponse from (PaymentInfo paymentInfo) {
            if (paymentInfo == null) {
                return null;
            }
            return PaymentResponse.builder()
                    .paymentId(paymentInfo.getPaymentId())
                    .paymentKey(paymentInfo.getPaymentKey())
                    .amount(paymentInfo.getAmount())
                    .status(paymentInfo.getStatus().name())
                    .method(paymentInfo.getMethod().name())
                    .approvedAt(paymentInfo.getApprovedAt().toString())
                    .build();
        }
    }
}
