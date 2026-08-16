package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.order.domain.order.event.OrderPaidEvent;
import com.example.order_service.saga.application.service.OrderSagaCommandService;
import com.example.order_service.saga.domain.OrderSagaPayload;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderListener {

    private final OrderSagaCommandService orderSagaCommandService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        CreateOrderSagaContext createContext = mapToSagaContext(event);
        orderSagaCommandService.createOrderSaga(createContext);
    }

    private CreateOrderSagaContext mapToSagaContext(OrderPaidEvent event) {
        List<OrderSagaPayload.OrderLine> orderLines = event.items().stream().map(item -> OrderSagaPayload.OrderLine.builder()
                .productVariantId(item.productVariantId())
                .quantity(item.quantity())
                .build()).toList();

        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(event.cartCouponId())
                .itemCouponIds(event.itemCouponIds())
                .build();

        OrderSagaPayload payload = OrderSagaPayload.builder()
                .userId(event.userId())
                .orderLines(orderLines)
                .usedCoupons(usedCoupons)
                .usedPoints(event.usedPoints())
                .build();

        return CreateOrderSagaContext.builder()
                .orderId(event.orderId())
                .payload(payload)
                .build();
    }
}
