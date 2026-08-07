package com.example.order_service.payment.application.service.dto.command;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;

@Deprecated
public class PaymentCommand {

    @Builder
    public record Confirm(
            Long userId,
            String orderNo,
            String paymentKey,
            Money amount
    ) {
    }
}
