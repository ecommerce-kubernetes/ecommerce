package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.OrderPriceInfo;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderDetailResponse {
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private String orderName;
    private String deliveryAddress;
    private OrderPriceResponse orderPriceResponse;
    private PaymentResponse paymentResponse;
    private CouponResponse couponResponse;
    private List<OrderItemResponse> orderItems;
    private String createdAt;

    @Builder
    private OrderDetailResponse(Long orderId, Long userId, String orderStatus, String orderName, String deliveryAddress,
                                OrderPriceResponse orderPriceResponse, CouponResponse couponResponse, PaymentResponse paymentResponse, List<OrderItemResponse> orderItems,
                                String createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.orderPriceResponse = orderPriceResponse;
        this.couponResponse = couponResponse;
        this.paymentResponse = paymentResponse;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
    }

    @Getter
    @NoArgsConstructor
    public static class OrderPriceResponse {
        private Long totalOriginPrice;
        private Long totalProductDiscount;
        private Long couponDiscount;
        private Long pointDiscount;
        private Long finalPaymentAmount;

        @Builder
        private OrderPriceResponse(Long totalOriginPrice, Long totalProductDiscount, Long couponDiscount, Long pointDiscount, Long finalPaymentAmount) {
            this.totalOriginPrice = totalOriginPrice;
            this.totalProductDiscount = totalProductDiscount;
            this.couponDiscount = couponDiscount;
            this.pointDiscount = pointDiscount;
            this.finalPaymentAmount = finalPaymentAmount;
        }

        public static OrderPriceResponse from(OrderPriceInfo orderPriceInfo) {
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
    @NoArgsConstructor
    public static class CouponResponse {
        private Long couponId;
        private String couponName;
        private Long couponDiscount;

        @Builder
        private CouponResponse(Long couponId, String couponName, Long couponDiscount) {
            this.couponId = couponId;
            this.couponName = couponName;
            this.couponDiscount = couponDiscount;
        }

        public static CouponResponse from(AppliedCoupon coupon) {
            return CouponResponse.builder()
                    .couponId(coupon.getCouponId())
                    .couponName(coupon.getCouponName())
                    .couponDiscount(coupon.getDiscountAmount())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentResponse {
        private Long paymentId;
        private String paymentKey;
        private Long amount;
        private String method;
        private String approvedAt;

        @Builder
        private PaymentResponse(Long paymentId, String paymentKey, Long amount, String method, String approvedAt) {
            this.paymentId = paymentId;
            this.paymentKey = paymentKey;
            this.amount = amount;
            this.method = method;
            this.approvedAt = approvedAt;
        }

        public static PaymentResponse from(PaymentInfo paymentInfo) {
            return PaymentResponse.builder()
                    .paymentId(paymentInfo.getId())
                    .paymentKey(paymentInfo.getPaymentKey())
                    .amount(paymentInfo.getAmount())
                    .method(paymentInfo.getMethod())
                    .approvedAt(paymentInfo.getApprovedAt().toString())
                    .build();
        }
    }

    public static OrderDetailResponse from(OrderDto orderDto) {
        List<OrderItemResponse> items = orderDto.getOrderItemDtoList().stream().map(OrderItemResponse::from).toList();
        return OrderDetailResponse.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .orderStatus(orderDto.getStatus().name())
                .orderName(orderDto.getOrderName())
                .deliveryAddress(orderDto.getDeliveryAddress())
                .orderPriceResponse(OrderPriceResponse.from(orderDto.getOrderPriceInfo()))
                .couponResponse(CouponResponse.from(orderDto.getAppliedCoupon()))
                .orderItems(items)
                .paymentResponse(PaymentResponse.from(orderDto.getPaymentInfo()))
                .createdAt(orderDto.getOrderedAt().toString())
                .build();
    }
}
