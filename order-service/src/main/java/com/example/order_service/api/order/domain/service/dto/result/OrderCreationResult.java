package com.example.order_service.api.order.domain.service.dto.result;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderCreationResult {
    private Long orderId;
    private String status;
    private String orderTitle;
    private LocalDateTime orderedAt;
    private PaymentInfo paymentInfo;
    private List<OrderItemDto> orderItemDtoList;
    private AppliedCoupon appliedCoupon;
}
