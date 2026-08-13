package com.example.order_service.saga.domain.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.saga.domain.OrderSaga;
import com.example.order_service.saga.domain.OrderSagaPayload;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;

import java.util.Collections;
import java.util.List;

public class OrderSagaFixtureBuilder {

    private Long orderId = 1L;
    private Long userId = 1L;
    private boolean hasCoupon = false;
    private Money usedPoints = Money.ZERO;

    private IdGenerator idGenerator = () -> 100L;

    public static OrderSagaFixtureBuilder given() {
        return new OrderSagaFixtureBuilder();
    }

    public OrderSagaFixtureBuilder withCoupon() {
        this.hasCoupon = true;
        return this;
    }

    public OrderSagaFixtureBuilder withPoints(long amount){
        this.usedPoints = Money.wons(amount);
        return this;
    }

    public OrderSagaFixtureBuilder withIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    public OrderSaga build() {
        OrderSagaPayload.UsedCoupons coupons = this.hasCoupon
                ? OrderSagaPayload.UsedCoupons.builder().cartCouponId(1L).itemCouponIds(List.of(2L)).build()
                : OrderSagaPayload.UsedCoupons.builder().cartCouponId(null).itemCouponIds(Collections.emptyList()).build();

        OrderSagaPayload payload = OrderSagaPayload.builder()
                .userId(this.userId)
                .orderLines(List.of(OrderSagaPayload.OrderLine.builder().productVariantId(999L).quantity(1).build()))
                .usedCoupons(coupons)
                .usedPoints(this.usedPoints)
                .build();

        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(this.orderId)
                .payload(payload)
                .build();

        return OrderSaga.create(context, this.idGenerator);
    }
}
