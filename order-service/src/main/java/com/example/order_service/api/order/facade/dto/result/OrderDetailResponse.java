package com.example.order_service.api.order.facade.dto.result;

import com.example.order_service.api.order.domain.model.vo.CouponInfo;
import com.example.order_service.api.order.domain.model.vo.OrderPriceDetail;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {
    private String orderNo;
    private String status;
    private String orderName;
    private OrdererResponse orderer;
    private OrderPriceResponse orderPrice;
    private String deliveryAddress;
    private PaymentResponse paymentResponse;
    private CouponResponse couponResponse;
    private List<OrderItemResponse> orderItems;
    private String createdAt;

    @Getter
    @Builder
    public static class OrderPriceResponse {
        private Long totalOriginPrice;
        private Long totalProductDiscount;
        private Long couponDiscount;
        private Long pointDiscount;
        private Long finalPaymentAmount;

        public static OrderPriceResponse from(OrderPriceDetail orderPrice) {
            return OrderPriceResponse.builder()
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

        public static OrdererResponse from(Orderer orderer) {
            return OrdererResponse.builder()
                    .userId(orderer.getUserId())
                    .userName(orderer.getUserName())
                    .phoneNumber(orderer.getPhoneNumber())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PaymentResponse {
        private Long paymentId;
        private String paymentKey;
        private Long amount;
        private String method;
        private String approvedAt;
    }

    public static OrderDetailResponse from(OrderDto orderDto) {
//        return OrderDetailResponse.builder()
//                .orderNo(orderDto.getOrderNo())
//                .userId(orderDto.getUserId())
//                .orderStatus(orderDto.getStatus().name())
//                .orderName(orderDto.getOrderName())
//                .orderer(OrdererResponse.from(orderDto.getOrderer()))
//                .orderPrice(OrderPriceResponse.from(orderDto.getOrderPriceDetail()))
//                .deliveryAddress(orderDto.getDeliveryAddress())
//                .
//                .build()

        return null;
    }
}
