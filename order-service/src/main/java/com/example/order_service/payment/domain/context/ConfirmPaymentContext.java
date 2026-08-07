package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import lombok.Builder;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Builder
public record ConfirmPaymentContext(
        PaymentMethod method,
        PaymentProvider provider,
        String paymentKey,
        String transactionKey,
        Money amount,
        LocalDateTime occurredAt
) {
    public ConfirmPaymentContext {
        Assert.notNull(method, "결제 승인시 결제 방법은 필수이다.");
        Assert.notNull(provider, "결제 승인시 결제사는 필수이다.");
        Assert.hasText(paymentKey, "결제 승인시 결제 키는 필수이다.");
        Assert.hasText(transactionKey, "결제 승인시 결제 트랜잭션 키는 필수이다.");
        Assert.notNull(amount, "결제 승인시 승인 금액 필수이다.");
        Assert.notNull(occurredAt, "결제 승인시 승인 시간은 필수이다.");
    }
}
