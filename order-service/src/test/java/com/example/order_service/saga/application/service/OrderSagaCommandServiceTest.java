package com.example.order_service.saga.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.domain.*;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
import com.example.order_service.saga.domain.event.UsedCouponEvent;
import com.example.order_service.saga.exception.SagaNotFoundException;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("주문 사가를 생성하고 재고 차감 이벤트를 발행한다.")
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

        long eventCount = events.stream(ReduceInventoryEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 사가 작업을 성공후 다음 스텝 이벤트를 발행한다.")
    void completeForward() {
        //given
        CreateOrderSagaContext context = createContext();
        Long orderSagaId = orderSagaCommandService.createOrderSaga(context);

        OrderSaga orderSaga = orderSagaRepository.findById(orderSagaId).orElseThrow();
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        orderSagaCommandService.completeForward(orderSagaId, execution.getId());
        //then
        long eventCount = events.stream(UsedCouponEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 사가를 찾을 수 없는 경우 예외가 발생한다.")
    void completeForward_whenNotFoundSaga_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaCommandService.completeForward(999L, 1L))
                .isInstanceOf(SagaNotFoundException.class);
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