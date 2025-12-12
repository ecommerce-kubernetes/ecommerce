package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderCreationResult {
    private Long orderId;
    private String status;
    private LocalDateTime orderedAt;
    private PaymentInfo paymentInfo;
    private List<OrderItemDto> orderItemDtoList;
    private AppliedCoupon appliedCoupon;

    @Builder
    private OrderCreationResult(Long orderId, String status, LocalDateTime orderedAt,
                                PaymentInfo paymentInfo, List<OrderItemDto> orderItemDtoList, AppliedCoupon appliedCoupon){
        this.orderId = orderId;
        this.status = status;
        this.orderedAt = orderedAt;
        this.paymentInfo = paymentInfo;
        this.orderItemDtoList = orderItemDtoList;
        this.appliedCoupon = appliedCoupon;
    }
}
