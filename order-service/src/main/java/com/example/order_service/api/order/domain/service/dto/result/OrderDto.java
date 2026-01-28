package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.*;
import com.example.order_service.api.order.domain.model.vo.OrderPriceDetail;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDto {
    private Long id;
    private String orderNo;
    private OrderStatus status;
    private String orderName;
    private OrdererInfo orderer;
    private OrderPriceInfo orderPriceInfo;
    private CouponInfo couponInfo;
    private List<OrderItemDto> orderItems;
    private String deliveryAddress;
    private PaymentInfo paymentInfo;
    private LocalDateTime orderedAt;
    private OrderFailureCode orderFailureCode;

    @Getter
    @Builder
    public static class OrdererInfo {
        private Long userId;
        private String userName;
        private String phoneNumber;

        public static OrdererInfo from(Orderer orderer) {
            return OrdererInfo.builder()
                    .userId(orderer.getUserId())
                    .userName(orderer.getUserName())
                    .phoneNumber(orderer.getPhoneNumber())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderPriceInfo {
        private long totalOriginPrice;
        private long totalProductDiscount;
        private long couponDiscount;
        private long pointDiscount;
        private long finalPaymentAmount;

        public static OrderPriceInfo from(OrderPriceDetail orderPrice) {
            return OrderPriceInfo.builder()
                    .totalOriginPrice(orderPrice.getTotalOriginPrice())
                    .totalProductDiscount(orderPrice.getTotalProductDiscount())
                    .couponDiscount(orderPrice.getCouponDiscount())
                    .pointDiscount(orderPrice.getPointDiscount())
                    .finalPaymentAmount(orderPrice.getFinalPaymentAmount())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class CouponInfo {
        private Long couponId;
        private String couponName;
        private Long discountAmount;

        public static CouponInfo from(Coupon coupon) {
            if (coupon == null) {
                return null;
            }
            return CouponInfo.builder()
                    .couponId(coupon.getCouponId())
                    .couponName(coupon.getCouponName())
                    .discountAmount(coupon.getDiscountAmount())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PaymentInfo {
        private Long paymentId;
        private String paymentKey;
        private Long amount;
        private PaymentType type;
        private PaymentStatus status;
        private PaymentMethod method;
        private LocalDateTime approvedAt;

        public static PaymentInfo from(Payment payment) {
            if (payment == null) {
                return null;
            }
            return PaymentInfo.builder()
                    .paymentId(payment.getId())
                    .paymentKey(payment.getPaymentKey())
                    .amount(payment.getAmount())
                    .type(payment.getType())
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .approvedAt(payment.getApprovedAt())
                    .build();
        }
    }

    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus())
                .orderName(order.getOrderName())
                .orderer(OrdererInfo.from(order.getOrderer()))
                .orderPriceInfo(OrderPriceInfo.from(order.getOrderPriceDetail()))
                .couponInfo(CouponInfo.from(order.getCoupon()))
                .orderItems(createOrderItemDto(order.getOrderItems()))
                .deliveryAddress(order.getDeliveryAddress())
                .paymentInfo(PaymentInfo.from(order.getValidPayment()))
                .orderedAt(order.getCreatedAt())
                .orderFailureCode(order.getFailureCode())
                .build();
    }

    private static List<OrderItemDto> createOrderItemDto(List<OrderItem> orderItems){
        return orderItems.stream().map(OrderItemDto::from).toList();
    }
}
