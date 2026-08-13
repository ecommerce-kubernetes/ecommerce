package com.example.order_service.saga.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.exception.ExecutionNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OrderSagaTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("주문 사가를 생성한다.")
    void create(){
        //given
        OrderSagaPayload payload = createPayload();
        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
        //when
        OrderSaga orderSaga = OrderSaga.create(context, idGenerator);
        //then
        assertThat(orderSaga.getId()).isNotNull();

        assertThat(orderSaga)
                .extracting("orderId", "status", "currentStep")
                .containsExactly(1L, SagaStatus.PROCESSING, SagaStep.INVENTORY);

        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(1)
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("주문 사가 생성시 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_idGenerator_null(){
        //given
        OrderSagaPayload payload = createPayload();
        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
        //when
        //then
        assertThatThrownBy(() -> OrderSaga.create(context, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 생성시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("주문 사가 생성시 아이디가 누락되면 예외가 발생한다.")
    void create_id_null(){
        //given
        OrderSagaPayload payload = createPayload();
        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> OrderSaga.create(context, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 생성시 아이디는 필수이다.");
    }

    @Test
    @DisplayName("스텝을 완료한다.")
    void completeStep() {
        //given
        OrderSagaPayload payload = createPayload();
        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
        OrderSaga orderSaga = OrderSaga.create(context, idGenerator);

        OrderSagaExecution execution = orderSaga.getOrderSagaExecutions().getFirst();
        //when
        orderSaga.completeStep(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.COUPON);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(2)
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.COUPON)
                );
    }

    @Test
    @DisplayName("스텝을 완료할때 작업을 찾을 수 없으면 예외가 발생한다.")
    void completeStep_notFound_execution() {
        //given
        OrderSagaPayload payload = createPayload();
        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
        OrderSaga orderSaga = OrderSaga.create(context, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> orderSaga.completeStep(999L, idGenerator))
                .isInstanceOf(ExecutionNotFoundException.class);
    }

    @Test
    @DisplayName("스텝을 실패 처리 한다.")
    void failStep() {
        //given
        OrderSagaPayload payload = createPayload();
        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
        OrderSaga orderSaga = OrderSaga.create(context, idGenerator);

        OrderSagaExecution execution = orderSaga.getOrderSagaExecutions().getFirst();
        //when
        orderSaga.failStep(execution.getId(), "재고 감소 실패", idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("스텝을 실패처리할 때 작업을 찾을 수 없으면 예외가 발생한다.")
    void failStep_notFound_execution() {
        //given
        OrderSagaPayload payload = createPayload();
        CreateOrderSagaContext context = CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
        OrderSaga orderSaga = OrderSaga.create(context, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> orderSaga.failStep(999L, "재고 감소 실패", idGenerator))
                .isInstanceOf(ExecutionNotFoundException.class);
    }

    private OrderSagaPayload createPayload() {
        OrderSagaPayload.OrderLine line = OrderSagaPayload.OrderLine.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(1L, 2L))
                .build();

        return OrderSagaPayload.builder()
                .userId(1L)
                .orderLines(List.of(line))
                .usedCoupons(usedCoupons)
                .usedPoints(Money.wons(1000L))
                .build();
    }
}