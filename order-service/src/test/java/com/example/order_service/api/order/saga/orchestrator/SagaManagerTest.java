package com.example.order_service.api.order.saga.orchestrator;

import com.example.common.result.SagaProcessResult;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaPaymentCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaResourceSecuredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.api.support.fixture.SagaManagerTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SagaManagerTest {

    @InjectMocks
    private SagaManager sagaManager;

    @Mock
    private SagaEventProducer sagaEventProducer;
    @Mock
    private OrderSagaDomainService orderSagaDomainService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<Payload> payloadCaptor;
    @Captor
    private ArgumentCaptor<SagaResourceSecuredEvent> sagaResourceSecuredCaptor;
    @Captor
    private ArgumentCaptor<SagaAbortEvent> sagaAbortCaptor;

    @Test
    @DisplayName("Saga 시작 시 인스턴스를 저장하고 재고 차감 요청 이벤트를 발행한다")
    void startSaga() {
        //given
        SagaStartCommand command = createStartCommand();
        SagaInstanceDto sagaInstance = createSagaInstance(SagaStep.PRODUCT, SagaStatus.STARTED, Payload.from(command));

        given(orderSagaDomainService.create(anyLong(), any(Payload.class), any(SagaStep.class)))
                .willReturn(sagaInstance);

        //when
        sagaManager.startSaga(command);
        //then
        verify(orderSagaDomainService, times(1))
                .create(
                        eq(ORDER_ID),
                        payloadCaptor.capture(),
                        eq(SagaStep.PRODUCT)
                );

        assertThat(payloadCaptor.getValue())
                .extracting(Payload::getUserId, Payload::getCouponId, Payload::getUseToPoint)
                .containsExactly(USER_ID, COUPON_ID, USE_POINT);
        assertThat(payloadCaptor.getValue().getSagaItems())
                .hasSize(2);

        verify(sagaEventProducer).requestInventoryDeduction(
                eq(SAGA_ID),
                eq(ORDER_ID),
                any(Payload.class)
        );
    }

    @Test
    @DisplayName("재고감소 성공 후 쿠폰과 포인트를 모두 사용한다면 다음 단계인 쿠폰 단계를 실행한다")
    void proceedSaga_inventory_success_with_coupon_point(){
        //given
        Payload payload = createDefaultPayload();
        mockSagaTransition(SagaStep.PRODUCT, SagaStep.COUPON, payload);
        //when
        sagaManager.processProductResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        // 상태 변경 검증
        verify(orderSagaDomainService).proceedTo(SAGA_ID, SagaStep.COUPON);
        // 이벤트 발행 검증
        verify(sagaEventProducer).requestCouponUse(SAGA_ID, ORDER_ID, payload);
        // 포인트 단계로 바로 실행 되면 안되고 SAGA 가 바로 종료되서는 안된다
        verify(sagaEventProducer, never()).requestUserPointUse(anyLong(), anyLong(), any(Payload.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("재고감소 성공 후 쿠폰은 사용하지 않고 포인트만 사용한다면 다음 단계인 쿠폰 단계를 건너뛰고 포인트 단계를 실행한다")
    void proceedSaga_inventory_success_skip_coupon(){
        //given
        Payload payload = createPayload(null, USE_POINT);
        mockSagaTransition(SagaStep.PRODUCT, SagaStep.USER, payload);
        //when
        sagaManager.processProductResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        //포인트 관련 로직은 실행되어야함
        verify(orderSagaDomainService, times(1)).proceedTo(SAGA_ID, SagaStep.USER);
        verify(sagaEventProducer, times(1)).requestUserPointUse(SAGA_ID, ORDER_ID, payload);

        // 쿠폰단계 또는 결제 대기 상태가 실행되서는 안됨
        verify(orderSagaDomainService, never()).proceedTo(SAGA_ID, SagaStep.COUPON);
        verify(sagaEventProducer, never()).requestCouponUse(anyLong(), anyLong(), any(Payload.class));
    }

    @Test
    @DisplayName("재고감소 성공 후 쿠폰과 포인트 모두 사용하지 않는다면 쿠폰 단계와 포인트 단계를 건너뛰고 결제 대기 단계를 실행한다")
    void proceedSaga_inventory_success_all_skip(){
        //given
        Payload payload = createPayload(null, NO_POINT);
        mockSagaTransition(SagaStep.PRODUCT, SagaStep.PAYMENT, payload);
        //when
        sagaManager.processProductResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        // 결제 대기로 상태 변경 확인
        verify(orderSagaDomainService, times(1)).proceedTo(SAGA_ID, SagaStep.PAYMENT);
        verify(eventPublisher, times(1)).publishEvent(sagaResourceSecuredCaptor.capture());
        assertThat(sagaResourceSecuredCaptor.getValue())
                .extracting(SagaResourceSecuredEvent::getSagaId, SagaResourceSecuredEvent::getOrderId, SagaResourceSecuredEvent::getUserId)
                        .containsExactly(SAGA_ID, ORDER_ID, USER_ID);

        // 쿠폰단계 또는 포인트 단계가 실행되서는 안됨
        verify(sagaEventProducer, never()).requestCouponUse(anyLong(), anyLong(), any(Payload.class));
        verify(sagaEventProducer, never()).requestUserPointUse(anyLong(), anyLong(), any(Payload.class));
    }

    @Test
    @DisplayName("쿠폰 사용 성공 후 포인트를 사용한다면 포인트단계를 진행한다")
    void proceedSaga_coupon_success_with_point(){
        //given
        Payload payload = createPayload(COUPON_ID, USE_POINT);
        mockSagaTransition(SagaStep.COUPON, SagaStep.USER, payload);
        //when
        sagaManager.processCouponResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        // 포인트 단계가 실행되어야 함
        verify(orderSagaDomainService, times(1)).proceedTo(SAGA_ID, SagaStep.USER);
        verify(sagaEventProducer, times(1)).requestUserPointUse(SAGA_ID, ORDER_ID, payload);

        // 이전 단계를 실행하면 안됨
        verify(sagaEventProducer, never()).requestCouponUse(any(), any(), any());
        // 결제 대기 상태로 바로 건너뛰면 안됨
        verify(eventPublisher, never()).publishEvent(any());
        // SAGA 를 끝내서는 안됨
        verify(orderSagaDomainService, never()).finish(anyLong());
    }

    @Test
    @DisplayName("쿠폰 사용 성공 후 포인트를 사용하지 않는다면 포인트단계를 건너뛰고 결제 대기 단계를 진행한다")
    void proceedSaga_coupon_success_skip_point(){
        //given
        Payload payload = createPayload(COUPON_ID, NO_POINT);
        mockSagaTransition(SagaStep.COUPON, SagaStep.PAYMENT, payload);
        //when
        sagaManager.processCouponResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        verify(orderSagaDomainService, times(1)).proceedTo(SAGA_ID, SagaStep.PAYMENT);
        verify(eventPublisher, times(1)).publishEvent(sagaResourceSecuredCaptor.capture());
        assertThat(sagaResourceSecuredCaptor.getValue())
                .extracting(SagaResourceSecuredEvent::getSagaId, SagaResourceSecuredEvent::getOrderId, SagaResourceSecuredEvent::getUserId)
                .containsExactly(SAGA_ID, ORDER_ID, USER_ID);

        // 쿠폰단계 또는 포인트 단계가 실행되서는 안됨
        verify(sagaEventProducer, never()).requestCouponUse(anyLong(), anyLong(), any(Payload.class));
        verify(sagaEventProducer, never()).requestUserPointUse(anyLong(), anyLong(), any(Payload.class));
    }

    @Test
    @DisplayName("포인트 차감 성공후 결제 대기 상태로 변경한다")
    void proceedSaga_point_success(){
        //given
        Payload payload = createPayload(COUPON_ID, USE_POINT);
        mockSagaTransition(SagaStep.USER, SagaStep.PAYMENT, payload);
        //when
        sagaManager.processUserResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        verify(orderSagaDomainService, times(1)).proceedTo(SAGA_ID, SagaStep.PAYMENT);
        verify(eventPublisher, times(1)).publishEvent(sagaResourceSecuredCaptor.capture());
        assertThat(sagaResourceSecuredCaptor.getValue())
                .extracting(SagaResourceSecuredEvent::getSagaId, SagaResourceSecuredEvent::getOrderId, SagaResourceSecuredEvent::getUserId)
                .containsExactly(SAGA_ID, ORDER_ID, USER_ID);

        // 쿠폰단계 또는 포인트 단계가 실행되서는 안됨
        verify(sagaEventProducer, never()).requestCouponUse(anyLong(), anyLong(), any(Payload.class));
        verify(sagaEventProducer, never()).requestUserPointUse(anyLong(), anyLong(), any(Payload.class));
    }

    @Test
    @DisplayName("포인트 차감이 실패한 경우 쿠폰을 사용했다면 쿠폰 보상을 진행한다")
    void processUserResult_point_fail_with_couponId() {
        //given
        Payload payload = createDefaultPayload();
        String errorCode = "INSUFFICIENT_POINT";
        String failureReason = "포인트가 부족합니다";

        mockCompensationStart(SagaStep.USER, SagaStep.COUPON, payload, failureReason);
        //when
        sagaManager.processUserResult(SagaProcessResult.fail(SAGA_ID, ORDER_ID, errorCode, failureReason));
        //then
        // 보상 시작 서비스 메서드 호출 검증
        verify(orderSagaDomainService).startCompensation(SAGA_ID, SagaStep.COUPON, failureReason);
        // 쿠폰 보상 메시지 발행 검증
        verify(sagaEventProducer).requestCouponCompensate(SAGA_ID, ORDER_ID, payload);
        // SAGA 중지 이벤트 발행 검증
        verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());

        assertThat(sagaAbortCaptor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                .containsExactly(SAGA_ID, ORDER_ID, USER_ID, OrderFailureCode.POINT_SHORTAGE);

        // 상품 보상으로 넘어가면 안됨
        verify(sagaEventProducer, never()).requestInventoryCompensate(anyLong(), anyLong(), any(Payload.class));
        //SAGA 가 종료되면 안됨
        verify(orderSagaDomainService, never()).fail(anyLong(), nullable(String.class));
    }

    @Test
    @DisplayName("포인트 차감이 실패한 경우 쿠폰을 사용하지 않았다면 상품 재고 보상을 진행한다")
    void processUserResult_point_fail_without_couponId(){
        //given
        Payload payload = createPayload(null, USE_POINT);
        String errorCode = "INSUFFICIENT_POINT";
        String failureReason = "포인트가 부족합니다";

        mockCompensationStart(SagaStep.USER, SagaStep.PRODUCT, payload, failureReason);
        //when
        sagaManager.processUserResult(SagaProcessResult.fail(SAGA_ID, ORDER_ID, errorCode, failureReason));
        //then
        // 상품 재고 보상 로직 실행 검증
        verify(orderSagaDomainService).startCompensation(SAGA_ID, SagaStep.PRODUCT, failureReason);
        verify(sagaEventProducer).requestInventoryCompensate(SAGA_ID, ORDER_ID, payload);

        // 쿠폰 보상은 실행되면 안됨
        verify(sagaEventProducer, never()).requestCouponCompensate(anyLong(), anyLong(), any());

        // SAGA 실패 이벤트 발행 검증
        verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
        assertThat(sagaAbortCaptor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                .containsExactly(SAGA_ID, ORDER_ID, USER_ID, OrderFailureCode.POINT_SHORTAGE);
    }

    @Test
    @DisplayName("쿠폰 사용이 실패한 경우 상품 보상을 진행한다")
    void processCouponResult_coupon_fail(){
        //given
        Payload payload = createDefaultPayload();
        String errorCode = "INVALID_COUPON";
        String failureReason = "유효하지 않은 쿠폰";
        mockCompensationStart(SagaStep.COUPON, SagaStep.PRODUCT, payload, failureReason);
        //when
        sagaManager.processCouponResult(SagaProcessResult.fail(SAGA_ID, ORDER_ID, errorCode, failureReason));
        //then
        verify(orderSagaDomainService).startCompensation(SAGA_ID, SagaStep.PRODUCT, failureReason);
        verify(sagaEventProducer).requestInventoryCompensate(SAGA_ID, ORDER_ID, payload);
        verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
        assertThat(sagaAbortCaptor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                .containsExactly(SAGA_ID, ORDER_ID, USER_ID, OrderFailureCode.INVALID_COUPON);
        // SAGA가 바로 종료되면 안됨
        verify(orderSagaDomainService, never()).fail(anyLong(), nullable(String.class));
    }

    @Test
    @DisplayName("재고감소가 실패한 경우 saga를 실패 처리한다")
    void processProductResult_inventory_fail(){
        //given
        Payload payload = createDefaultPayload();
        String errorCode = "OUT_OF_STOCK";
        String failureReason = "재고 부족";
        mockImmediateFailure(SagaStep.PRODUCT, payload);
        //when
        sagaManager.processProductResult(SagaProcessResult.fail(SAGA_ID, ORDER_ID, errorCode, failureReason));
        //then
        verify(orderSagaDomainService).fail(SAGA_ID, failureReason);

        // 보상이 발생해서는 안됨
        verify(sagaEventProducer, never()).requestInventoryCompensate(anyLong(), anyLong(), any());

        verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
        assertThat(sagaAbortCaptor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                .containsExactly(SAGA_ID, ORDER_ID, USER_ID, OrderFailureCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("쿠폰 보상이 완료된 이후 상품 재고 보상을 진행한다")
    void compensateSaga_coupon_success(){
        //given
        Payload payload = createDefaultPayload();
        mockCompensationTransition(SagaStep.COUPON, SagaStep.PRODUCT, payload);
        //when
        sagaManager.processCouponResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        //상품 보상 로직이 진행되어야 함
        verify(orderSagaDomainService, times(1)).continueCompensation(SAGA_ID, SagaStep.PRODUCT);
        verify(sagaEventProducer, times(1)).requestInventoryCompensate(SAGA_ID, ORDER_ID, payload);

        //쿠폰 복구는 진행되서는 안됨
        verify(sagaEventProducer, never()).requestCouponCompensate(anyLong(), anyLong(), any());
        //SAGA 가 완료되서는 안됨, Saga 실패 이벤트가 발행되서는 안됨
        verify(orderSagaDomainService, never()).fail(anyLong(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("상품 재고 보상이 완료되면 Saga를 마친다")
    void compensateSaga_inventory_success(){
        //given
        Payload payload = createDefaultPayload();
        mockCompensateEnd(SagaStep.PRODUCT, payload);
        //when
        sagaManager.processProductResult(SagaProcessResult.success(SAGA_ID, ORDER_ID));
        //then
        //사가가 완료됨
        verify(orderSagaDomainService).fail(SAGA_ID, null);

        //다른 단계는 진행되서는 안됨
        verify(sagaEventProducer, never()).requestInventoryCompensate(anyLong(), anyLong(), any());
        verify(sagaEventProducer, never()).requestCouponCompensate(anyLong(), anyLong(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("타임아웃시 타임아웃된 SAGA를 조회하고 보상을 수행한다")
    void processTimeouts(){
        //given
        Payload payload = createDefaultPayload();
        Long sagaId1 = 10L;
        Long sagaId2 = 20L;
        SagaInstanceDto saga1 = createSagaInstanceWithId(sagaId1, SagaStep.COUPON, SagaStatus.STARTED, payload);
        SagaInstanceDto saga2 = createSagaInstanceWithId(sagaId2, SagaStep.COUPON, SagaStatus.STARTED, payload);

        given(orderSagaDomainService.getTimeouts(any(LocalDateTime.class)))
                .willReturn(List.of(saga1, saga2));

        mockCompensationReturn(sagaId1, SagaStep.PRODUCT, payload);
        mockCompensationReturn(sagaId2, SagaStep.PRODUCT, payload);
        //when
        sagaManager.processTimeouts();
        //then
        verify(orderSagaDomainService).startCompensation(eq(sagaId1), any(), eq("주문 처리 지연"));
        verify(orderSagaDomainService).startCompensation(eq(sagaId2), any(), eq("주문 처리 지연"));

        verify(sagaEventProducer).requestInventoryCompensate(eq(sagaId1), any(), eq(payload));
        verify(sagaEventProducer).requestInventoryCompensate(eq(sagaId2), any(), eq(payload));
    }

    @Test
    @DisplayName("결제 승인 완료시 Saga 종료")
    void processPaymentResult(){
        //given
        Payload payload = createDefaultPayload();
        SagaPaymentCommand command = createPaymentSuccessCommand();
        mockSagaFinishByOrder(SagaStep.PAYMENT, payload);
        //when
        sagaManager.processPaymentResult(command);
        //then
        verify(orderSagaDomainService, times(1)).finish(SAGA_ID);

        verify(orderSagaDomainService, never()).fail(anyLong(), anyString());
    }

    @Test
    @DisplayName("결제 승인실패시 포인트를 사용한 경우 포인트 보상을 진행한다")
    void processPaymentResult_paymentFail_with_usedPoint(){
        //given
        Payload payload = createDefaultPayload();
        String failureReason = "결제 승인 실패";
        SagaPaymentCommand command = createPaymentFailCommand(failureReason);
        mockCompensationStartByOrder(SagaStep.PAYMENT, SagaStep.USER, payload, failureReason);
        //when
        sagaManager.processPaymentResult(command);
        //then
        verify(orderSagaDomainService).startCompensation(SAGA_ID, SagaStep.USER, failureReason);
        verify(sagaEventProducer).requestUserPointCompensate(SAGA_ID, ORDER_ID, payload);

        verify(sagaEventProducer, never()).requestCouponCompensate(anyLong(), anyLong(), any());
        verify(sagaEventProducer, never()).requestCouponUse(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("결제 승인 실패시 포인트를 사용하지 않고 쿠폰을 사용했다면 포인트 보상을 건너뛰고 쿠폰 보상을 진행한다")
    void processPaymentResult_paymentFail_skip_user(){
        //given
        Payload payload = createPayload(COUPON_ID, NO_POINT);
        String failureReason = "결제 승인 실패";
        SagaPaymentCommand command = createPaymentFailCommand(failureReason);
        mockCompensationStartByOrder(SagaStep.PAYMENT, SagaStep.COUPON, payload, failureReason);
        //when
        sagaManager.processPaymentResult(command);
        //then
        verify(orderSagaDomainService).startCompensation(SAGA_ID, SagaStep.COUPON, failureReason);

        verify(sagaEventProducer).requestCouponCompensate(SAGA_ID, ORDER_ID, payload);

        verify(sagaEventProducer, never()).requestUserPointCompensate(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("결제 승인 실패시 포인트를 사용하지 않고 쿠폰도 사용하지 않았다면 포인트 보상과 쿠폰 보상을 건너뛰고 상품 보상을 진행한다")
    void processPaymentResult_paymentFail_skip_user_and_coupon(){
        //given
        Payload payload = createPayload(null, NO_POINT);
        String failureReason = "결제 승인 실패";

        SagaPaymentCommand command = createPaymentFailCommand(failureReason);
        mockCompensationStartByOrder(SagaStep.PAYMENT, SagaStep.PRODUCT, payload, failureReason);
        //when
        sagaManager.processPaymentResult(command);
        //then
        verify(orderSagaDomainService).startCompensation(SAGA_ID, SagaStep.PRODUCT, failureReason);
        verify(sagaEventProducer).requestInventoryCompensate(SAGA_ID, ORDER_ID, payload);

        verify(sagaEventProducer, never()).requestUserPointCompensate(anyLong(), anyLong(), any());
        verify(sagaEventProducer, never()).requestCouponCompensate(anyLong(), anyLong(), any());
    }

    private void mockSagaTransition(SagaStep currentStep, SagaStep nextStep, Payload payload) {
        SagaInstanceDto currentInstance = createSagaInstance(currentStep, SagaStatus.STARTED, payload);
        SagaInstanceDto nextInstance = createSagaInstance(nextStep, SagaStatus.STARTED, payload);

        given(orderSagaDomainService.getSagaBySagaId(SAGA_ID))
                .willReturn(currentInstance);
        given(orderSagaDomainService.proceedTo(SAGA_ID, nextStep))
                .willReturn(nextInstance);
    }

    private void mockCompensationStart(SagaStep currentStep, SagaStep compensateStep, Payload payload, String failureReason) {
        SagaInstanceDto currentInstance = createSagaInstance(currentStep, SagaStatus.STARTED, payload);
        SagaInstanceDto compensateInstance = createSagaInstance(compensateStep, SagaStatus.COMPENSATING, payload);
        given(orderSagaDomainService.getSagaBySagaId(SAGA_ID))
                .willReturn(currentInstance);
        given(orderSagaDomainService.startCompensation(SAGA_ID, compensateStep, failureReason))
                .willReturn(compensateInstance);
    }

    private void mockImmediateFailure(SagaStep step, Payload payload) {
        SagaInstanceDto currentInstance = createSagaInstance(step, SagaStatus.STARTED, payload);
        SagaInstanceDto failedInstance = createSagaInstance(step, SagaStatus.FAILED, payload);
        given(orderSagaDomainService.getSagaBySagaId(SAGA_ID))
                .willReturn(currentInstance);
        given(orderSagaDomainService.fail(eq(SAGA_ID), anyString()))
                .willReturn(failedInstance);
    }

    private void mockCompensationTransition(SagaStep currentStep, SagaStep nextStep, Payload payload) {
        SagaInstanceDto currentInstance = createSagaInstance(currentStep, SagaStatus.COMPENSATING, payload);
        SagaInstanceDto nextInstance = createSagaInstance(nextStep, SagaStatus.COMPENSATING, payload);
        given(orderSagaDomainService.getSagaBySagaId(SAGA_ID))
                .willReturn(currentInstance);
        given(orderSagaDomainService.continueCompensation(SAGA_ID, nextStep))
                .willReturn(nextInstance);
    }

    private void mockCompensateEnd(SagaStep currentStep, Payload payload) {
        SagaInstanceDto currentInstance = createSagaInstance(currentStep, SagaStatus.COMPENSATING, payload);
        SagaInstanceDto failedInstance = createSagaInstance(currentStep, SagaStatus.FAILED, payload);
        given(orderSagaDomainService.getSagaBySagaId(SAGA_ID))
                .willReturn(currentInstance);
        given(orderSagaDomainService.fail(eq(SAGA_ID), isNull()))
                .willReturn(failedInstance);
    }

    private void mockCompensationReturn(Long sagaId, SagaStep nextStep, Payload payload) {
        SagaInstanceDto compensatingInstance = createSagaInstanceWithId(sagaId, nextStep, SagaStatus.COMPENSATING, payload);
        given(orderSagaDomainService.startCompensation(eq(sagaId), any(SagaStep.class), anyString()))
                .willReturn(compensatingInstance);
    }

    private void mockSagaFinishByOrder(SagaStep step, Payload payload) {
        SagaInstanceDto currentInstance = createSagaInstance(step, SagaStatus.STARTED, payload);
        SagaInstanceDto finishedInstance = createSagaInstance(step, SagaStatus.FINISHED, payload);
        given(orderSagaDomainService.getSagaByOrderId(ORDER_ID))
                .willReturn(currentInstance);
        given(orderSagaDomainService.finish(SAGA_ID))
                .willReturn(finishedInstance);
    }

    private void mockCompensationStartByOrder(SagaStep currentStep, SagaStep compensateStep, Payload payload, String failureReason) {
        SagaInstanceDto currentInstance = createSagaInstance(currentStep, SagaStatus.STARTED, payload);
        SagaInstanceDto compensateInstance = createSagaInstance(compensateStep, SagaStatus.COMPENSATING, payload);
        given(orderSagaDomainService.getSagaByOrderId(ORDER_ID))
                .willReturn(currentInstance);
        given(orderSagaDomainService.startCompensation(SAGA_ID, compensateStep, failureReason))
                .willReturn(compensateInstance);
    }
}
