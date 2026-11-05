package com.example.order_service.order.domain.saga;

import com.example.order_service.order.domain.vo.SagaPayload;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSagaInstanceTest {

    @Test
    @DisplayName("saga 인스턴스를 생성한다")
    void create() {
        //given
        String orderNo = "orderNo";
        Long paymentId = 1L;
        SagaStep step = SagaStep.INVENTORY_RESTORE_PENDING;
        SagaPayload sagaPayload = Instancio.create(SagaPayload.class);
        //when
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, step, sagaPayload);
        //then
        assertThat(instance)
                .extracting("orderNo", "paymentId", "currentStep", "status")
                .containsExactly(
                        orderNo, paymentId, step, SagaStatus.STARTED
                );
        assertThat(instance.getPayload())
                .usingRecursiveComparison()
                .isEqualTo(sagaPayload);
    }

    @Test
    @DisplayName("saga History를 추가한다")
    void addHistory() {
        //given
        String orderNo = "orderNo";
        Long paymentId = 1L;
        SagaStep step = SagaStep.INVENTORY_RESTORE_PENDING;
        SagaPayload sagaPayload = Instancio.create(SagaPayload.class);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, step, sagaPayload);

        SagaStepHistory history = Instancio.create(SagaStepHistory.class);
        //when
        instance.addHistory(history);
        //then
        assertThat(instance.getHistories()).hasSize(1);
    }

    @Test
    @DisplayName("saga Step을 다음 스텝으로 변경한다")
    void proceedTo(){
        //given
        String orderNo = "orderNo";
        Long paymentId = 1L;
        SagaStep step = SagaStep.INVENTORY_DEDUCT_PENDING;
        SagaStep nextStep = SagaStep.COUPON_USE_PENDING;
        SagaPayload sagaPayload = Instancio.create(SagaPayload.class);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, step, sagaPayload);
        //when
        instance.proceedTo(nextStep);
        //then
        assertThat(instance)
                .extracting("status", "currentStep")
                .containsExactly(
                        SagaStatus.STARTED, SagaStep.COUPON_USE_PENDING
                );
    }

    @Test
    @DisplayName("saga 상태를 보상 중으로 변경하고 다음 스텝을 변경한다")
    void compensateTo(){
        //given
        String orderNo = "orderNo";
        Long paymentId = 1L;
        SagaStep step = SagaStep.COUPON_USE_PENDING;
        SagaStep nextStep = SagaStep.INVENTORY_RESTORE_PENDING;
        SagaPayload sagaPayload = Instancio.create(SagaPayload.class);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, step, sagaPayload);
        //when
        instance.compensateTo(nextStep);
        //then
        assertThat(instance)
                .extracting("status", "currentStep")
                .containsExactly(
                        SagaStatus.COMPENSATING, SagaStep.INVENTORY_RESTORE_PENDING
                );
    }

    @Test
    @DisplayName("saga를 완료 처리한다")
    void complete() {
        //given
        String orderNo = "orderNo";
        Long paymentId = 1L;
        SagaStep step = SagaStep.INVENTORY_RESTORE_PENDING;
        SagaPayload sagaPayload = Instancio.create(SagaPayload.class);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, step, sagaPayload);
        //when
        instance.complete();
        //then
        assertThat(instance)
                .extracting("status", "currentStep")
                .containsExactly(
                        SagaStatus.COMPLETE, SagaStep.END
                );
    }

    @Test
    @DisplayName("saga를 실패 처리한다")
    void failed() {
        //given
        String orderNo = "orderNo";
        Long paymentId = 1L;
        SagaStep step = SagaStep.INVENTORY_RESTORE_PENDING;
        SagaPayload sagaPayload = Instancio.create(SagaPayload.class);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, step, sagaPayload);
        //when
        instance.failed();
        //then
        assertThat(instance)
                .extracting("status", "currentStep")
                .containsExactly(
                        SagaStatus.FAILED, SagaStep.END
                );
    }
}