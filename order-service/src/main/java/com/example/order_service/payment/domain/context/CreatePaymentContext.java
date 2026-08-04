package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Builder
public record CreatePaymentContext (
        Long orderId,
        Long userId,
        Money totalAmount
){
}
