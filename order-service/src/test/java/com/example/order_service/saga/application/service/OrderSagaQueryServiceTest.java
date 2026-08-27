package com.example.order_service.saga.application.service;

import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.saga.application.port.OrderSagaRepository;
import com.example.order_service.saga.application.service.dto.result.OrderSagaExecutionResult;
import com.example.order_service.saga.domain.ExecutionStatus;
import com.example.order_service.saga.domain.ExecutionType;
import com.example.order_service.saga.domain.OrderSaga;
import com.example.order_service.saga.domain.OrderSagaExecution;
import com.example.order_service.saga.domain.SagaStep;
import com.example.order_service.saga.domain.fixture.OrderSagaFixtureBuilder;
import com.example.order_service.support.annotation.IsolatedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedTest
@Transactional
class OrderSagaQueryServiceTest {

    @Autowired
    private OrderSagaQueryService orderSagaQueryService;

    @Autowired
    private OrderSagaRepository orderSagaRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("타임아웃된 정방향 대기중인 사가 작업을 조회한다.")
    void getForwardPendingExecutionsBefore() {
        //given
        OrderSaga orderSaga1 = OrderSagaFixtureBuilder.given().build();
        OrderSaga orderSaga2 = OrderSagaFixtureBuilder.given().build();

        orderSagaRepository.save(orderSaga1);
        orderSagaRepository.save(orderSaga2);
        flushAndClear();

        OrderSagaExecution execution1 = orderSaga1.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);

        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
        em.createNativeQuery("UPDATE order_saga_execution SET updated_at = :pastTime WHERE id = :id")
                .setParameter("pastTime", pastTime)
                .setParameter("id", execution1.getId())
                .executeUpdate();

        flushAndClear();

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        //when
        List<OrderSagaExecutionResult> result = orderSagaQueryService.getForwardPendingExecutionsBefore(timeoutThreshold);
        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("sagaId")
                .containsExactlyInAnyOrder(orderSaga1.getId());
    }

    @Test
    @DisplayName("타임아웃 대상 정방향 대기중인 사가 작업이 없으면 빈 목록을 반환한다.")
    void getForwardPendingExecutionsBefore_whenNotExistTimeoutExecutions_thenReturnEmpty() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().build();
        orderSagaRepository.save(orderSaga);
        flushAndClear();

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        //when
        List<OrderSagaExecutionResult> result = orderSagaQueryService.getForwardPendingExecutionsBefore(timeoutThreshold);
        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("타임아웃된 보상 대기중인 사가 작업을 조회한다.")
    void getCompensatePendingExecutionsBefore() {
        //given
        OrderSaga orderSaga1 = OrderSagaFixtureBuilder.given()
                .withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        OrderSaga orderSaga2 = OrderSagaFixtureBuilder.given()
                .withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");

        orderSagaRepository.save(orderSaga1);
        orderSagaRepository.save(orderSaga2);
        flushAndClear();

        OrderSagaExecution execution1 = orderSaga1.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);

        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
        em.createNativeQuery("UPDATE order_saga_execution SET updated_at = :pastTime WHERE id = :id")
                .setParameter("pastTime", pastTime)
                .setParameter("id", execution1.getId())
                .executeUpdate();

        flushAndClear();

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        //when
        List<OrderSagaExecutionResult> result = orderSagaQueryService.getCompensatePendingExecutionsBefore(timeoutThreshold);
        //then
        assertThat(result).hasSize(1);
        assertThat(result).extracting("sagaId")
                .containsExactlyInAnyOrder(orderSaga1.getId());
    }

    @Test
    @DisplayName("타임아웃 대상 보상 대기중인 사가 작업이 없으면 빈 목록을 반환한다.")
    void getCompensatePendingExecutionsBefore_whenNotExistTimeoutExecutions_thenReturnEmpty() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given()
                .withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        orderSagaRepository.save(orderSaga);
        flushAndClear();

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        //when
        List<OrderSagaExecutionResult> result = orderSagaQueryService.getCompensatePendingExecutionsBefore(timeoutThreshold);
        //then
        assertThat(result).isEmpty();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
