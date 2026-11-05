package com.example.order_service.saga.domain;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.saga.domain.context.CreateOrderSagaContext;
import com.example.order_service.saga.domain.fixture.OrderSagaFixtureBuilder;
import com.example.order_service.saga.exception.SagaErrorCode;
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
        CreateOrderSagaContext context = createContext();
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
        CreateOrderSagaContext context = createContext();
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
        CreateOrderSagaContext context = createContext();
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> OrderSaga.create(context, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 사가 생성시 아이디는 필수이다.");
    }

    @Test
    @DisplayName("재고 차감 완료 후 쿠폰을 사용했다면 쿠폰 스텝으로 이동한다.")
    void completeForward_afterInventory_whenCouponUsed_thenMoveToCouponStep() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndForwardTo(SagaStep.INVENTORY);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        orderSaga.completeForward(execution.getId(), idGenerator);
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
    @DisplayName("재고 차감 완료 후 쿠폰을 사용하지 않고 포인트를 사용했다면 포인트 스텝으로 이동한다.")
    void completeForward_afterInventory_whenNoCouponAndPointUsed_thenMoveToPointStep() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withPoints(1000L).buildAndForwardTo(SagaStep.INVENTORY);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        orderSaga.completeForward(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.POINT);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(2)
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.POINT)
                );
    }

    @Test
    @DisplayName("재고 차감 완료 후 쿠폰과 포인트를 모두 사용하지 않았다면 사가를 완료한다.")
    void completeForward_afterInventory_whenNoCouponAndNoPointUsed_thenComplete() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().buildAndForwardTo(SagaStep.INVENTORY);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        orderSaga.completeForward(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPLETE);
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.END);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(1)
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("쿠폰 무효화 후 포인트를 사용했다면 포인트 스텝으로 이동한다.")
    void completeForward_afterCoupon_whenUsedPoint_thenMoveToPointStep(){
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().withPoints(1000L)
                .buildAndForwardTo(SagaStep.COUPON);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.COUPON);
        //when
        orderSaga.completeForward(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.PROCESSING);
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.POINT);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(3)
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.COUPON),
                        tuple(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.POINT)
                );
    }

    @Test
    @DisplayName("쿠폰 무효화 후 포인트를 사용하지 않았다면 사가를 완료한다.")
    void completeForward_afterCoupon_whenNotUsedPoint_thenComplete(){
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndForwardTo(SagaStep.COUPON);

        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.COUPON);
        //when
        orderSaga.completeForward(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPLETE);
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.END);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(2)
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.COUPON)
                );
    }

    @Test
    @DisplayName("포인트 차감을 완료하면 사가를 완료 한다.")
    void completeForward_afterPoint_thenComplete(){
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withPoints(1000).buildAndForwardTo(SagaStep.POINT);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.POINT);
        //when
        orderSaga.completeForward(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPLETE);
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.END);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(2)
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.POINT)
                );
    }

    @Test
    @DisplayName("스텝을 완료할때 작업을 찾을 수 없으면 예외가 발생한다.")
    void completeForward_whenExecutionNotFound_thenThrowException() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().build();
        //when
        //then
        assertThatThrownBy(() -> orderSaga.completeForward(999L, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_EXECUTION);
    }
    
    @Test
    @DisplayName("완료된 정방향 작업을 다시 완료해도 중복 처리되지 않는다")
    void completeForward_idempotency() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndForwardTo(SagaStep.INVENTORY);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);

        orderSaga.completeForward(execution.getId(), idGenerator);
        int expectedSize = orderSaga.getOrderSagaExecutions().size();
        //when
        orderSaga.completeForward(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(expectedSize);
    }

    @Test
    @DisplayName("재고 차감 실패 후 사가는 철회된다.")
    void failForward_afterInventory_thenAbort() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().buildAndForwardTo(SagaStep.INVENTORY);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);
        //when
        orderSaga.failForward(execution.getId(), "재고 부족", idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.ABORT);
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("쿠폰 무효화 실패 후 재고 보상을 진행한다.")
    void failForward_afterCoupon_thenMoveToInventoryCompensate(){
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndForwardTo(SagaStep.COUPON);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.COUPON);
        //when
        orderSaga.failForward(execution.getId(), "쿠폰 만료", idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.COUPON),
                        tuple(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("포인트 차감 실패후 쿠폰을 무효화 했다면 쿠폰 보상을 진행한다.")
    void failForward_afterPoint_whenCouponUsed_thenMoveToCouponCompensate() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().withPoints(1000L).buildAndForwardTo(SagaStep.POINT);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.POINT);
        //when
        orderSaga.failForward(execution.getId(), "포인트 부족", idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.COUPON),
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.POINT),
                        tuple(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.COUPON)
                );
    }

    @Test
    @DisplayName("포인트 차감 실패 후 쿠폰을 무효화 하지 않았다면 재고 보상을 진행한다.")
    void failForward_afterPoint_whenNoCoupon_thenMoveToInventoryCompensate() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withPoints(1000L).buildAndForwardTo(SagaStep.POINT);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.POINT);
        //when
        orderSaga.failForward(execution.getId(), "포인트 부족", idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.POINT),
                        tuple(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("스텝을 실패할 때 작업을 찾을 수 없으면 예외가 발생한다.")
    void failForward_whenExecutionNotFound_thenThrowException() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().buildAndForwardTo(SagaStep.INVENTORY);
        //when
        //then
        assertThatThrownBy(() -> orderSaga.failForward(999L, "재고 감소 실패", idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_EXECUTION);
    }
    
    @Test
    @DisplayName("실패한 정방향 작업을 다시 실패해도 중복 처리 되지 않는")
    void failForward_idempotency() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().withPoints(1000L).buildAndForwardTo(SagaStep.POINT);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.POINT);

        orderSaga.failForward(execution.getId(), "포인트 부족", idGenerator);
        int expectedSize = orderSaga.getOrderSagaExecutions().size();
        //when
        orderSaga.failForward(execution.getId(), "포인트 부족", idGenerator);
        //then
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(expectedSize);
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
    }

    @Test
    @DisplayName("재고 복구 후 사가는 실패한다.")
    void completeCompensate_afterInventory_thenAbort() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);
        //when
        orderSaga.completeCompensate(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.ABORT);
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.COUPON),
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.COMPENSATE, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("쿠폰 복구 후 재고 복구를 진행한다.")
    void completeCompensate_afterCoupon_thenMoveToInventoryCompensate() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().withPoints(1000).buildAndFailAt(SagaStep.POINT, "포인트 부족");

        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.COUPON);
        //when
        orderSaga.completeCompensate(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.COUPON),
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.POINT),
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.COMPENSATE, SagaStep.COUPON),
                        tuple(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("복구할 작업을 찾을 수 없으면 예외가 발생한다.")
    void completeCompensate_whenExecutionNotFound_thenThrowException() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        //when
        //then
        assertThatThrownBy(() -> orderSaga.completeCompensate(999L, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_EXECUTION);
    }

    @Test
    @DisplayName("완료된 롤백을 다시 완료 처리해도 중복 처리 되지 않는다")
    void completeCompensate_idempotency() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);

        orderSaga.completeCompensate(execution.getId(), idGenerator);
        int expectedSize = orderSaga.getOrderSagaExecutions().size();
        //when
        orderSaga.completeCompensate(execution.getId(), idGenerator);
        //then
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(expectedSize);
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.ABORT);
    }

    @Test
    @DisplayName("복구 실패시 사가를 실패한다.")
    void failCompensate() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);
        //when
        orderSaga.failCompensate(execution.getId());
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(orderSaga.getCurrentStep()).isEqualTo(SagaStep.END);
        assertThat(orderSaga.getOrderSagaExecutions())
                .extracting("status", "type", "step")
                .containsExactly(
                        tuple(ExecutionStatus.SUCCESS, ExecutionType.FORWARD, SagaStep.INVENTORY),
                        tuple(ExecutionStatus.FAIL, ExecutionType.FORWARD, SagaStep.COUPON),
                        tuple(ExecutionStatus.FAIL, ExecutionType.COMPENSATE, SagaStep.INVENTORY)
                );
    }

    @Test
    @DisplayName("복구를 실패할때 실패할 작업을 찾을 수 없으면 예외가 발생한다.")
    void failCompensate_whenExecutionNotFound_thenThrowException() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        //when
        //then
        assertThatThrownBy(() -> orderSaga.failCompensate(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_EXECUTION);
    }
    
    @Test
    @DisplayName("실패한 롤백 작업을 다시 실패 처리해도 중복 처리 되지 않는다")
    void failCompensate_idempotency() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);

        orderSaga.failCompensate(execution.getId());
        int expectedSize = orderSaga.getOrderSagaExecutions().size();
        //when
        orderSaga.failCompensate(execution.getId());
        //then
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(expectedSize);
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.FAILED);
    }

    @Test
    @DisplayName("보상 대기중인 작업을 재시도해도 사가와 작업의 상태는 변경되지 않는다.")
    void retryCompensate_whenPendingCompensateExecution_thenStateUnchanged() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);
        int executionCountBefore = orderSaga.getOrderSagaExecutions().size();
        //when
        orderSaga.retryCompensate(execution.getId());
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(executionCountBefore);
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.PENDING);
    }

    @Test
    @DisplayName("이미 완료된 보상 작업을 재시도해도 상태가 변경되지 않는다.")
    void retryCompensate_whenExecutionAlreadySucceeded_thenDoNothing() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.COMPENSATE, SagaStep.INVENTORY);
        orderSaga.completeCompensate(execution.getId(), idGenerator);

        SagaStatus statusBefore = orderSaga.getStatus();
        int executionCountBefore = orderSaga.getOrderSagaExecutions().size();
        //when
        orderSaga.retryCompensate(execution.getId());
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(statusBefore);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(executionCountBefore);
    }

    @Test
    @DisplayName("정방향 대기 작업을 재시도해도 상태가 변경되지 않는다.")
    void retryCompensate_whenExecutionIsForwardType_thenDoNothing() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().buildAndForwardTo(SagaStep.INVENTORY);
        OrderSagaExecution execution = orderSaga.getExecution(ExecutionStatus.PENDING, ExecutionType.FORWARD, SagaStep.INVENTORY);

        SagaStatus statusBefore = orderSaga.getStatus();
        int executionCountBefore = orderSaga.getOrderSagaExecutions().size();
        //when
        orderSaga.retryCompensate(execution.getId());
        //then
        assertThat(orderSaga.getStatus()).isEqualTo(statusBefore);
        assertThat(orderSaga.getOrderSagaExecutions()).hasSize(executionCountBefore);
    }

    @Test
    @DisplayName("재시도할 작업을 찾을 수 없으면 예외가 발생한다.")
    void retryCompensate_whenExecutionNotFound_thenThrowException() {
        //given
        OrderSaga orderSaga = OrderSagaFixtureBuilder.given().withCoupon().buildAndFailAt(SagaStep.COUPON, "쿠폰 만료");
        //when
        //then
        assertThatThrownBy(() -> orderSaga.retryCompensate(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.NOT_FOUND_EXECUTION);
    }

    private CreateOrderSagaContext createContext() {
        OrderSagaPayload.UsedCoupons usedCoupons = OrderSagaPayload.UsedCoupons.builder()
                .cartCouponId(1L)
                .itemCouponIds(List.of(2L, 3L))
                .build();

        OrderSagaPayload.OrderLine line = OrderSagaPayload.OrderLine.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        OrderSagaPayload payload = OrderSagaPayload.builder()
                .userId(1L)
                .orderLines(List.of(line))
                .usedCoupons(usedCoupons)
                .usedPoints(Money.wons(1000L))
                .build();

        return CreateOrderSagaContext.builder()
                .orderId(1L)
                .payload(payload)
                .build();
    }
}