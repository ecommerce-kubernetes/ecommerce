package com.example.order_service.saga.domain;

import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class OrderSagaExecutionTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("주문 사가 작업을 생성한다.")
    void create() {
        //given
        //when
        OrderSagaExecution execution = OrderSagaExecution.create(idGenerator, SagaStep.INVENTORY);
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
        assertThatThrownBy(() -> OrderSagaExecution.create(null, SagaStep.INVENTORY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 작업 생성시 아이디 생성기는 필수이다.");
    }

    @Test
    @DisplayName("주문 사가 작업 생성시 사가 단계가 누락되면 예외가 발생한다.")
    void create_step_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> OrderSagaExecution.create(idGenerator, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 작업 생성시 사가 단계는 필수이다.");
    }

    @Test
    @DisplayName("주문 사가 작업 생성시 아이디가 누락되면 예외가 발생한다.")
    void create_id_null(){
        //given
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> OrderSagaExecution.create(nullIdGenerator, SagaStep.INVENTORY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 작업 생성시 아이디는 필수이다.");
    }
}