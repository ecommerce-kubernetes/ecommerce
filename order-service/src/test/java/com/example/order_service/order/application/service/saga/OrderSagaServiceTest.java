package com.example.order_service.order.application.service.saga;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.event.OrderSagaCompletedEvent;
import com.example.order_service.order.application.event.OrderSagaFailedEvent;
import com.example.order_service.order.application.event.OrderSagaProcessEvent;
import com.example.order_service.order.application.service.saga.dto.OrderSagaCommand;
import com.example.order_service.order.application.service.saga.dto.OrderSagaResult;
import com.example.order_service.order.domain.repository.OrderSagaInstanceRepository;
import com.example.order_service.order.domain.saga.OrderSagaInstance;
import com.example.order_service.order.domain.saga.SagaStatus;
import com.example.order_service.order.domain.saga.SagaStep;
import com.example.order_service.order.domain.saga.StepResult;
import com.example.order_service.order.domain.vo.SagaPayload;
import com.example.order_service.order.exception.SagaErrorCode;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@MockKafka
@MockRedis
@Transactional
@RecordApplicationEvents
public class OrderSagaServiceTest {

    @Autowired
    private OrderSagaService orderSagaService;
    @Autowired
    private OrderSagaInstanceRepository repository;
    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    @DisplayName("정방향 SagaInstance를 저장하고 Saga 실행 이벤트를 발행한다")
    void createSaga() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        Long paymentId = 1L;
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
        SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
        SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
        OrderSagaCommand.Create command = OrderSagaCommand.Create.of(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        //when
        orderSagaService.createSaga(command);
        //then
        OrderSagaInstance instance = repository.findByOrderNo(orderNo).orElseThrow();
        long eventCount = applicationEvents.stream(OrderSagaProcessEvent.class).count();
        assertThat(instance.getOrderNo()).isEqualTo(orderNo);
        assertThat(instance.getStatus()).isEqualTo(SagaStatus.STARTED);
        assertThat(instance.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
        assertThat(instance.getHistories()).isEmpty();
        assertThat(eventCount).isEqualTo(1);
        OrderSagaProcessEvent processEvent = applicationEvents.stream(OrderSagaProcessEvent.class).findFirst().orElseThrow();
        assertThat(processEvent.getOrderNo()).isEqualTo(orderNo);
        assertThat(processEvent.getStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
    }

    @Test
    @DisplayName("SagaInstance를 조회한다")
    void getSaga() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        Long paymentId = 1L;
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
        SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
        SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        repository.save(instance);
        //when
        OrderSagaResult.Default result = orderSagaService.getSaga(instance.getId());
        //then
        assertThat(result.orderNo()).isEqualTo(orderNo);
        assertThat(result.status()).isEqualTo(SagaStatus.STARTED);
        assertThat(result.currentStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
    }

    @Test
    @DisplayName("SagaInstance 조회시 SagaInstance를 찾을 수 없으면 예외가 발생한다")
    void getSaga_instance_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderSagaService.getSaga(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_INSTANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("Saga History를 저장하고 SagaStep을 변경후 Saga 실행 이벤트를 발행한다")
    void proceed() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        Long paymentId = 1L;
        SagaStep nextStep = SagaStep.COUPON_USE_PENDING;
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.COMPLETED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_SUCCESS");
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
        SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
        SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        repository.save(instance);
        //when
        orderSagaService.proceed(instance.getId(), nextStep, command);
        //then
        OrderSagaInstance findInstance = repository.findByOrderNo(orderNo).orElseThrow();
        assertThat(findInstance.getOrderNo()).isEqualTo(orderNo);
        assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.STARTED);
        assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.COUPON_USE_PENDING);
        assertThat(findInstance.getHistories()).hasSize(1)
                .extracting("step", "result")
                .containsExactlyInAnyOrder(
                        tuple(SagaStep.INVENTORY_DEDUCT_PENDING, StepResult.COMPLETED)
                );
        long eventCount = applicationEvents.stream(OrderSagaProcessEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        OrderSagaProcessEvent processEvent = applicationEvents.stream(OrderSagaProcessEvent.class).findFirst().orElseThrow();
        assertThat(processEvent.getOrderNo()).isEqualTo(orderNo);
        assertThat(processEvent.getStep()).isEqualTo(SagaStep.COUPON_USE_PENDING);
    }

    @Test
    @DisplayName("process 호출시 SagaInstance를 찾을 수 없는 경우 예외가 발생한다")
    void proceed_instance_not_found() {
        //given
        String orderNo = "orderNo";
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.COMPLETED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_SUCCESS");
        //when
        //then
        assertThatThrownBy(() -> orderSagaService.proceed(999L, SagaStep.COUPON_USE_PENDING, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_INSTANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("Saga History를 저장하고 SagaStatus와 Step을 변경 후 Saga 보상 이벤트를 발행한다")
    void compensate(){
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        Long paymentId = 1L;
        SagaStep nextStep = SagaStep.INVENTORY_RESTORE_PENDING;
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.FAILED,
                SagaStep.COUPON_USE_PENDING, "COUPON_NOT_FOUND");
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
        SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
        SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.COUPON_USE_PENDING, payload);
        repository.save(instance);
        //when
        orderSagaService.compensate(instance.getId(), nextStep, command);
        //then
        OrderSagaInstance findInstance = repository.findByOrderNo(orderNo).orElseThrow();
        assertThat(findInstance.getOrderNo()).isEqualTo(orderNo);
        assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_RESTORE_PENDING);
        assertThat(findInstance.getHistories()).hasSize(1)
                .extracting("step", "result")
                .containsExactlyInAnyOrder(
                        tuple(SagaStep.COUPON_USE_PENDING, StepResult.FAILED)
                );
        long eventCount = applicationEvents.stream(OrderSagaProcessEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        OrderSagaProcessEvent processEvent = applicationEvents.stream(OrderSagaProcessEvent.class).findFirst().orElseThrow();
        assertThat(processEvent.getOrderNo()).isEqualTo(orderNo);
        assertThat(processEvent.getStep()).isEqualTo(SagaStep.INVENTORY_RESTORE_PENDING);
    }

    @Test
    @DisplayName("compensate 호출시 instance를 찾을 수 없으면 예외가 발생한다")
    void compensate_instance_not_found(){
        //given
        String orderNo = "orderNo";
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.FAILED,
                SagaStep.COUPON_USE_PENDING, "NOT_FOUND_COUPON");
        //when
        //then
        assertThatThrownBy(() -> orderSagaService.compensate(999L, SagaStep.INVENTORY_RESTORE_PENDING, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_INSTANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("Saga History를 저장하고 Saga를 완료 처리후 Saga 완료 이벤트를 발행한다")
    void complete() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        Long paymentId = 1L;
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.COMPLETED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_SUCCESS");
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
        SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
        SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        repository.save(instance);
        //when
        orderSagaService.complete(instance.getId(), command);
        //then
        OrderSagaInstance findInstance = repository.findByOrderNo(orderNo).orElseThrow();
        assertThat(findInstance.getOrderNo()).isEqualTo(orderNo);
        assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.COMPLETE);
        assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.END);
        assertThat(findInstance.getHistories()).hasSize(1)
                .extracting("step", "result")
                .containsExactlyInAnyOrder(
                        tuple(SagaStep.INVENTORY_DEDUCT_PENDING, StepResult.COMPLETED)
                );
        long eventCount = applicationEvents.stream(OrderSagaCompletedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        OrderSagaCompletedEvent completedEvent = applicationEvents.stream(OrderSagaCompletedEvent.class).findFirst().orElseThrow();
        assertThat(completedEvent.getOrderNo()).isEqualTo(orderNo);
    }

    @Test
    @DisplayName("complete 호출시 instance를 찾을 수 없으면 예외가 발생한다")
    void complete_instance_not_found() {
        //given
        String orderNo = "orderNo";
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.COMPLETED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_SUCCESS");
        //when
        //then
        assertThatThrownBy(() -> orderSagaService.complete(999L, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_INSTANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("Saga History를 저장하고 Saga를 실패 처리 후 Saga 실패 이벤트를 발행한다")
    void fail() {
        //given
        String orderNo = "orderNo";
        Long userId = 1L;
        Long paymentId = 1L;
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.FAILED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_FAIL");
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
        SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
        SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        repository.save(instance);
        //when
        orderSagaService.fail(instance.getId(), command);
        //then
        OrderSagaInstance findInstance = repository.findByOrderNo(orderNo).orElseThrow();
        assertThat(findInstance.getOrderNo()).isEqualTo(orderNo);
        assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.END);
        assertThat(findInstance.getHistories()).hasSize(1)
                .extracting("step", "result")
                .containsExactlyInAnyOrder(
                        tuple(SagaStep.INVENTORY_DEDUCT_PENDING, StepResult.FAILED)
                );
        long eventCount = applicationEvents.stream(OrderSagaFailedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        OrderSagaFailedEvent failedEvent = applicationEvents.stream(OrderSagaFailedEvent.class).findFirst().orElseThrow();
        assertThat(failedEvent.getOrderNo()).isEqualTo(orderNo);
        assertThat(failedEvent.getPaymentId()).isEqualTo(paymentId);
        assertThat(failedEvent.getCode()).isEqualTo("INVENTORY_DEDUCT_FAIL");
    }

    @Test
    @DisplayName("fail 호출시 instance를 찾을 수 없으면 예외가 발생한다")
    void fail_instance_not_found() {
        //given
        String orderNo = "orderNo";
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.FAILED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_FAIL");
        //when
        //then
        assertThatThrownBy(() -> orderSagaService.fail(999L, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_INSTANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("history를 저장한다")
    void recordHistory() {
        //given
        String orderNo = "orderNo";
        Long paymentId = 1L;
        Long userId = 1L;
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.COMPLETED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_SUCCESS");
        SagaPayload.CouponPayload couponPayload = SagaPayload.CouponPayload.of(1L, List.of(1L, 2L));
        SagaPayload.ItemPayload itemPayload = SagaPayload.ItemPayload.of(1L, 1);
        SagaPayload.PointPayload pointPayload = SagaPayload.PointPayload.of(Money.wons(1000L));
        SagaPayload payload = SagaPayload.of(userId, List.of(itemPayload), couponPayload, pointPayload);
        OrderSagaInstance instance = OrderSagaInstance.create(orderNo, paymentId, SagaStep.INVENTORY_DEDUCT_PENDING, payload);
        repository.save(instance);
        //when
        orderSagaService.recordHistory(instance.getId(), command);
        //then
        OrderSagaInstance findInstance = repository.findByOrderNo(orderNo).orElseThrow();
        assertThat(findInstance.getOrderNo()).isEqualTo(orderNo);
        assertThat(findInstance.getStatus()).isEqualTo(SagaStatus.STARTED);
        assertThat(findInstance.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_DEDUCT_PENDING);
    }

    @Test
    @DisplayName("history를 저장할때 instance를 찾을 수 없으면 예외가 발생한다")
    void recordHistory_instance_not_found() {
        //given
        String orderNo = "orderNo";
        OrderSagaCommand.RecordHistory command = OrderSagaCommand.RecordHistory.of(orderNo, StepResult.COMPLETED,
                SagaStep.INVENTORY_DEDUCT_PENDING, "INVENTORY_DEDUCT_SUCCESS");
        //when
        //then
        assertThatThrownBy(() -> orderSagaService.recordHistory(999L, command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SagaErrorCode.SAGA_INSTANCE_NOT_FOUND);
    }
}
