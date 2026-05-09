package com.example.order_service.order.application.dto.result;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.model.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

public class OrderResult {

    @Builder
    public record Create(
            String orderNo,
            OrderStatus status,
            String orderName,
            Money finalPaymentAmount,
            LocalDateTime createdAt
    ) {}
}
