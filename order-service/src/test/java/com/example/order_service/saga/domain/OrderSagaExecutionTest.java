package com.example.order_service.saga.domain;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.saga.exception.SagaErrorCode;
import com.example.order_service.saga.exception.SagaSystemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSagaExecutionTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("주문 사가 작업을 생성한다.")
    void create() {
        //given
        //when
        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //then
        assertThat(execution.getId()).isNotNull();
        assertThat(execution)
                .extracting("status", "type", "step")
                .containsExactly(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);
    }

    @Test
    @DisplayName("주문 사가 작업 생성시 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_idGenerator_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> OrderSagaExecution.create(null, ExecutionType.FORWARD, SagaStep.INVENTORY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 작업 생성시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("주문 사가 작업 생성시 사가 단계가 누락되면 예외가 발생한다.")
    void create_step_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 작업 생성시 사가 단계는 필수이다.");
    }

    @Test
    @DisplayName("주문 사가 작업 생성시 아이디가 누락되면 예외가 발생한다.")
    void create_id_null() {
        //given
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> OrderSagaExecution.create(nullIdGenerator, ExecutionType.FORWARD, SagaStep.INVENTORY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 작업 생성시 아이디는 필수이다.");
    }

    @Test
    @DisplayName("사가 작업을 완료한다.")
    void success() {
        //given
        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        execution.success();
        //then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
    }

    @Test
    @DisplayName("사가 작업을 완료할때 상태가 FAIL 이면 예외가 발생한다.")
    void success_fail() {
        //given
        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.INVENTORY);
        execution.fail();
        //when
        //then
        assertThatThrownBy(execution::success)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.ALREADY_FAILED_EXECUTION);
    }

    @Test
    @DisplayName("사가 작업을 실패로 변경한다.")
    void fail() {
        //given
        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        execution.fail();
        //then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAIL);
    }

    @Test
    @DisplayName("사가 작업을 실패로 변경할때 SUCCESS이면 예외가 발생한다.")
    void fail_success() {
        //given
        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, ExecutionType.FORWARD, SagaStep.INVENTORY);
        execution.success();
        //when
        //then
        assertThatThrownBy(execution::fail)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.ALREADY_SUCCEED_EXECUTION);
    }
}