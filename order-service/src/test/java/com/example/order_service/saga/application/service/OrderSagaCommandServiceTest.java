package com.example.order_service.saga.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.domain.OrderSaga;
import com.example.order_service.saga.domain.OrderSagaPayload;
import com.example.order_service.saga.domain.SagaStatus;
import com.example.order_service.saga.domain.SagaStep;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@IsolatedTest
@Transactional
@RecordApplicationEvents
class OrderSagaCommandServiceTest {

    @Autowired
    private OrderSagaCommandService orderSagaCommandService;

    @Autowired
    private OrderSagaRepository orderSagaRepository;

    @Autowired
    private ApplicationEvents events;

    @Test
    @DisplayName("주문 사가를 생성한다.")
    void createOrderSaga() {
        //given
        CreateOrderSagaContext context = createContext();
        //when
        Long orderSagaId = orderSagaCommandService.createOrderSaga(context);
        //then
        OrderSaga orderSaga = orderSagaRepository.findById(orderSagaId).orElseThrow();

        assertThat(orderSaga.getId()).isNotNull();
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.PROCESSING);
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.INVENTORY);
    }

    @Test
    @DisplayName("주문 사가를 생성하면 재고 차감 이벤트가 발행된다.")
    void createOrderSaga_thenPublishReduceInventoryEvent() {
        //given
        CreateOrderSagaContext context = createContext();
        //when
        orderSagaCommandService.createOrderSaga(context);
        //then
        long eventCount = events.stream(ReduceInventoryEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    private CreateOrderSagaContext createContext() {
        OrderSagaPayload.OrderLine orderLine = OrderSagaPayload.OrderLine.builder()
                .productVariantId(1L)
                .quantity(1)
                .build();

        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(2L, 3L))
                .build();

        OrderSagaPayload payload = OrderSagaPayload.builder()
                .userId(1L)
                .orderLines(List.of(orderLine))
                .usedCoupons(usedCoupons)
                .usedPoints(Money.wons(1000L))
                .build();

        return CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
    }
}