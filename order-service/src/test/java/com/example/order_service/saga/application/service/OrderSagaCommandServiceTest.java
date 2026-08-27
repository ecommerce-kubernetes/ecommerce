package com.example.order_service.saga.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.domain.*;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
import com.example.order_service.saga.domain.event.RestoreInventoryEvent;
import com.example.order_service.saga.domain.event.SagaProcessingFailedEvent;
import com.example.order_service.saga.domain.event.UsedCouponEvent;
import com.example.order_service.saga.domain.fixture.OrderSagaFixtureBuilder;
import com.example.order_service.saga.exception.SagaErrorCode;
import com.example.order_service.saga.exception.SagaSystemException;
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
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_SAGA);
    }

    @Test
    @DisplayName("주문 사가 작업이 실패하고 롤백 대상이 없으면 사가를 철회하고 실패 이벤트를 발행한다.")
    void failForward() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().build();
        orderSagaRepository.save(orderSaga);

        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        orderSagaCommandService.failForward(orderSaga.getId(), execution.getId(), "재고 부족");
        //then
        OrderSaga findSaga = orderSagaRepository.findById(orderSaga.getId()).orElseThrow();
        assertThat(findSaga.getStatus()).isEqualTo(SagaStatus.ABORT);
        assertThat(findSaga.getFailureReason()).isEqualTo("재고 부족");

        long eventCount = events.stream(SagaProcessingFailedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 사가를 찾을 수 없는 경우 예외가 발생한다.")
    void failForward_whenNotFoundSaga_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaCommandService.failForward(999L, 1L, "실패"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_SAGA);
    }

    @Test
    @DisplayName("보상 작업을 완료하고 다음 보상 대상이 없으면 사가를 철회한다.")
    void completeCompensate() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        orderSagaRepository.save(orderSaga);

        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);
        //when
        orderSagaCommandService.completeCompensate(orderSaga.getId(), execution.getId());
        //then
        OrderSaga findSaga = orderSagaRepository.findById(orderSaga.getId()).orElseThrow();
        assertThat(findSaga.getStatus()).isEqualTo(SagaStatus.ABORT);
        assertThat(findSaga.getExecution(ExecutionStatus.SUCCESS, ExecutionType.COMPENSATE, SagaStep.INVENTORY)).isNotNull();
    }

    @Test
    @DisplayName("주문 사가를 찾을 수 없는 경우 예외가 발생한다.")
    void completeCompensate_whenNotFoundSaga_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaCommandService.completeCompensate(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_SAGA);
    }

    @Test
    @DisplayName("보상 작업이 실패하면 사가를 실패 처리한다.")
    void failCompensate() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        orderSagaRepository.save(orderSaga);

        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);
        //when
        orderSagaCommandService.failCompensate(orderSaga.getId(), execution.getId());
        //then
        OrderSaga findSaga = orderSagaRepository.findById(orderSaga.getId()).orElseThrow();
        assertThat(findSaga.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(findSaga.getExecution(ExecutionStatus.FAIL, ExecutionType.COMPENSATE, SagaStep.INVENTORY)).isNotNull();
    }

    @Test
    @DisplayName("주문 사가를 찾을 수 없는 경우 예외가 발생한다.")
    void failCompensate_whenNotFoundSaga_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaCommandService.failCompensate(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_SAGA);
    }

    @Test
    @DisplayName("보상 대기중인 작업을 재시도하면 보상 이벤트를 다시 발행한다.")
    void retryCompensate() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        orderSagaRepository.save(orderSaga);

        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);
        long eventCountBefore = events.stream(RestoreInventoryEvent.class).count();
        //when
        orderSagaCommandService.retryCompensate(orderSaga.getId(), execution.getId());
        //then
        long eventCountAfter = events.stream(RestoreInventoryEvent.class).count();
        assertThat(eventCountAfter).isEqualTo(eventCountBefore + 1);
    }

    @Test
    @DisplayName("주문 사가를 찾을 수 없는 경우 예외가 발생한다.")
    void retryCompensate_whenNotFoundSaga_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaCommandService.retryCompensate(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_SAGA);
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