package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Builder
public record CancelPaymentContext(
        String transactionKey,
        Money amount,
        String cancelReason,
        LocalDateTime occurredAt
) {

    public CancelPaymentContext {
        Assert.hasText(transactionKey, "결제 환불시 결제 트랜잭션 키는 필수이다.");
        Assert.notNull(amount, "결제 환불시 환불 금액은 필수이다.");
        Assert.hasText(cancelReason, "결제 환불시 환불 사유는 필수이다.");
        Assert.notNull(occurredAt, "결제 환불시 환불 시간은 필수이다.");
    }

}
