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
            @NotEmpty(message = "주문 번호는 필수입니다")
            String orderNo,
            @NotEmpty(message = "결제 키는 필수입니다")
            String paymentKey,
            @NotNull(message = "결제 금액은 필수입니다")
            @Min(value = 1, message = "결제 금액은 1원 미만일 수 없습니다")
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
