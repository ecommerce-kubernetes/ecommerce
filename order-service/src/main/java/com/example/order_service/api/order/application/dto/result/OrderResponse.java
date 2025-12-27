package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private String orderName;
    private String deliveryAddress;
    private PaymentResponse paymentResponse;
    private CouponResponse couponResponse;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;

    @Builder
    private OrderResponse(Long orderId, Long userId, String orderStatus, String orderName, String deliveryAddress,
                          PaymentResponse paymentResponse, CouponResponse couponResponse, List<OrderItemResponse> orderItems,
                          LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.paymentResponse = paymentResponse;
        this.couponResponse = couponResponse;
        this.orderItems = orderItems;
        this.createdAt = createdAt;
    }

    @Getter
    @NoArgsConstructor
    public static class PaymentResponse {
        private Long totalOriginPrice;
        private Long totalProductDiscount;
        private Long couponDiscount;
        private Long pointDiscount;
        private Long finalPaymentAmount;

        @Builder
        private PaymentResponse(Long totalOriginPrice, Long totalProductDiscount, Long couponDiscount, Long pointDiscount, Long finalPaymentAmount) {
            this.totalOriginPrice = totalOriginPrice;
            this.totalProductDiscount = totalProductDiscount;
            this.couponDiscount = couponDiscount;
            this.pointDiscount = pointDiscount;
            this.finalPaymentAmount = finalPaymentAmount;
        }

        public static PaymentResponse from(PaymentInfo paymentInfo) {
            return PaymentResponse.builder()
                    .totalOriginPrice(paymentInfo.getTotalOriginPrice())
                    .totalProductDiscount(paymentInfo.getTotalProductDiscount())
                    .couponDiscount(paymentInfo.getCouponDiscount())
                    .pointDiscount(paymentInfo.getUsedPoint())
                    .finalPaymentAmount(paymentInfo.getFinalPaymentAmount())
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

    public static OrderResponse from(OrderDto orderDto) {
        List<OrderItemResponse> items = orderDto.getOrderItemDtoList().stream().map(OrderItemResponse::from).toList();
        return OrderResponse.builder()
                .orderId(orderDto.getOrderId())
                .userId(orderDto.getUserId())
                .orderStatus(orderDto.getStatus().name())
                .orderName(orderDto.getOrderName())
                .deliveryAddress(orderDto.getDeliveryAddress())
                .paymentResponse(PaymentResponse.from(orderDto.getPaymentInfo()))
                .couponResponse(CouponResponse.from(orderDto.getAppliedCoupon()))
                .orderItems(items)
                .createdAt(orderDto.getOrderedAt())
                .build();
    }
}
