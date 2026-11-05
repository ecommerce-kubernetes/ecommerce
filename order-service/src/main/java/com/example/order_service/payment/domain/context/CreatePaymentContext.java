package com.example.order_service.payment.domain.context;

import com.example.order_service.common.domain.vo.Money;
import lombok.Builder;
import org.springframework.util.Assert;

@Builder
public record CreatePaymentContext (
        Long orderId,
        Long userId,
        Money totalAmount
){
    public CreatePaymentContext {
        Assert.notNull(orderId, "결제 생성시 주문 아이디는 필수이다.");
        Assert.notNull(userId, "결제 생성시 유저 아이디는 필수이다.");
        Assert.notNull(totalAmount, "결제 생성시 결제 금액은 필수이다.");
    }
}
