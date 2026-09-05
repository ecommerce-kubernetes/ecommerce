package com.example.order_service.payment.api.web.dto.request;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class PaymentRequest {

    @Builder(toBuilder = true)
    public record Confirm(
            @NotEmpty(message = "{payment.orderNo}")
            String orderNo,
            @NotEmpty(message = "{payment.paymentKey}")
            String paymentKey,
            @NotNull(message = "{payment.amount}")
            @Min(value = 1, message = "{payment.amount.min}")
            Long amount
    ) {
        public PaymentCommand.Confirm toCommand(Long userId) {
            return PaymentCommand.Confirm.builder()
                    .userId(userId)
                    .orderNo(orderNo)
                    .paymentKey(paymentKey)
                    .amount(Money.wons(amount))
                    .build();
        }
    }
}
