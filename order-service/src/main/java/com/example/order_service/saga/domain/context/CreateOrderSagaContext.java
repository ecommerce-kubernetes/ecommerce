package com.example.order_service.saga.domain.context;

import com.example.order_service.saga.domain.OrderSagaPayload;
import lombok.Builder;
import org.springframework.util.Assert;

@Builder
public record CreateOrderSagaContext(
        Long orderId,
        OrderSagaPayload payload
) {

    public CreateOrderSagaContext {
        Assert.notNull(orderId, "주문 사가 생성시 주문 아이디는 필수이다.");
        Assert.notNull(payload, "주문 사가 생성시 페이로드는 필수이다.");
    }
}
