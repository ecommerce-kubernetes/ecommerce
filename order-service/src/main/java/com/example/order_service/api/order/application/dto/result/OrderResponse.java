package com.example.order_service.api.order.application.dto.result;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private String orderStatus;
    private String orderName;
    private String deliveryAddress;
    private PaymentInfo paymentInfo;
    private List<OrderItemResponse> orderItems;

    public static class PaymentInfo {
        private Long totalOriginPrice;
        private Long totalProductDiscount;
        private Long couponDiscount;
        private Long pointDiscount;
        private Long finalPaymentAmount;
    }

}
