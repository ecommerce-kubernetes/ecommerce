package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OrderFixtureBuilder {

    private static AtomicLong idSeq = new AtomicLong(100L);
    private IdGenerator idGenerator = idSeq::getAndIncrement;

    private Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
    private ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
            "12345", "서울시 테헤란로 123", "123동 1234호");

    private TargetState targetState = TargetState.PENDING;
    private OrderCancelInfo cancelInfo;

    private enum TargetState { PENDING, ACCEPTED, COMPLETED, FAILED }

    public static OrderFixtureBuilder given() {
        return new OrderFixtureBuilder();
    }

    public OrderFixtureBuilder asAccepted() {
        this.targetState = TargetState.ACCEPTED;
        return this;
    }

    public OrderFixtureBuilder asCompleted() {
        this.targetState = TargetState.COMPLETED;
        return this;
    }

    public OrderFixtureBuilder asFailed(String reason) {
        this.targetState = TargetState.FAILED;
        this.cancelInfo = OrderCancelInfo.of(reason, LocalDateTime.now());
        return this;
    }

    public Order build() {
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.ZERO,
                Money.ZERO,
                Money.ZERO,
                Money.wons(27000L)
        );

        CreateOrderContext context = CreateOrderContext.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .appliedCartCoupon(null)
                .items(List.of(orderItemContext))
                .orderAmount(orderAmount)
                .build();

        Order order = Order.create(context, idGenerator);

        if (targetState == TargetState.PENDING) {
            return order;
        }

        if (targetState == TargetState.FAILED) {
            order.failed(this.cancelInfo);
            return order;
        }

        order.accept();
        if (targetState == TargetState.ACCEPTED) {
            return order;
        }

        order.complete();
        return order;
    }

    private CreateOrderItemContext createOrderItemContext() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        int quantity = 3;
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.ZERO, Money.wons(27000L));
        return CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(null)
                .quantity(quantity)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();
    }
}
