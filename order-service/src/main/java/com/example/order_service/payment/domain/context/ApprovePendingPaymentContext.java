package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.domain.PaymentProvider;
import lombok.Builder;
import org.springframework.util.Assert;

@Builder
public record ApprovePendingPaymentContext(
        Money amount,
        PaymentProvider provider,
        String paymentKey
) {

    public ApprovePendingPaymentContext {
        Assert.notNull(amount, "결제 승인 대기시 승인 금액은 필수이다.");
        Assert.notNull(provider, "결제 승인 대기시 결제사는 필수이다.");
        Assert.hasText(paymentKey, "결제 승인 대기시 결제 키는 필수이다.");
    }
}
