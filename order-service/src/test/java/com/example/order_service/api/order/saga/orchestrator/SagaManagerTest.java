package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.SagaService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStepResultCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.handler.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static com.example.order_service.api.support.fixture.saga.SagaManagerTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SagaManagerTest {

    @InjectMocks
    private SagaManager sagaManager;

    @Mock
    private SagaService sagaService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SagaStepHandlerFactory handlerFactory;

    @Captor
    private ArgumentCaptor<Payload> payloadCaptor;
    @Captor
    private ArgumentCaptor<SagaAbortEvent> sagaAbortCaptor;

    @Nested
    @DisplayName("Saga 시작")
    class Start {
        @Test
        @DisplayName("Saga 시작 시 PRODUCT 핸들러를 찾아 process를 호출한다")
        void startSaga() {
            //given
            SagaStartCommand command = anSagaStartCommand().build();
            SagaInstanceDto sagaInstanceDto = anSagaInstanceDto().build();
            ProductStepHandler mockHandler = mock(ProductStepHandler.class);
            given(sagaService.initialize(anyString(), any(Payload.class), any(SagaStep.class)))
                    .willReturn(sagaInstanceDto);
            given(handlerFactory.getHandler(SagaStep.PRODUCT))
                    .willReturn(mockHandler);
            //when
            sagaManager.startSaga(command);
            //then
            verify(sagaService).initialize(
                    eq(ORDER_NO),
                    payloadCaptor.capture(),
                    eq(SagaStep.PRODUCT)
            );

            assertThat(payloadCaptor.getValue())
                    .extracting(Payload::getUserId, Payload::getCouponId, Payload::getUseToPoint)
                    .containsExactly(1L, 1L, 1000L);
            assertThat(payloadCaptor.getValue().getSagaItems())
                    .hasSize(1);

            verify(mockHandler).process(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }

    @Nested
    @DisplayName("재고 감소 성공")
    class SuccessProductStockDeduct {

        @Test
        @DisplayName("재고감소 성공 후 쿠폰과 포인트를 모두 사용한다면 다음 단계인 쿠폰 단계를 실행한다")
        void proceedSaga_inventory_success_with_coupon_point(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto().sagaStatus(SagaStatus.STARTED).sagaStep(SagaStep.PRODUCT).build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto().sagaStatus(SagaStatus.STARTED).sagaStep(SagaStep.COUPON).build();
            CouponStepHandler mockHandler = mock(CouponStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.proceedTo(SAGA_ID, SagaStep.COUPON)).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.COUPON))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PRODUCT, ORDER_NO, true, null, null);
            //when
            sagaManager.handleStepResult(command);
            //then
            // 상태 변경 검증
            verify(sagaService).proceedTo(SAGA_ID, SagaStep.COUPON);
            // 이벤트 발행 검증
            verify(mockHandler).process(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }

        @Test
        @DisplayName("재고감소 성공 후 쿠폰은 사용하지 않고 포인트만 사용한다면 다음 단계인 쿠폰 단계를 건너뛰고 포인트 단계를 실행한다")
        void proceedSaga_inventory_success_skip_coupon(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PRODUCT)
                    .payload(anPayload().couponId(null).build())
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.USER)
                    .payload(anPayload().couponId(null).build())
                    .build();
            UserStepHandler mockHandler = mock(UserStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.proceedTo(SAGA_ID, SagaStep.USER)).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.USER))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PRODUCT, ORDER_NO, true, null, null);
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).proceedTo(SAGA_ID, SagaStep.USER);
            verify(mockHandler).process(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }

        @Test
        @DisplayName("재고감소 성공 후 쿠폰과 포인트 모두 사용하지 않는다면 쿠폰 단계와 포인트 단계를 건너뛰고 결제 대기 단계를 실행한다")
        void proceedSaga_inventory_success_all_skip(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PRODUCT)
                    .payload(anPayload().couponId(null).useToPoint(0L).build())
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PAYMENT)
                    .payload(anPayload().couponId(null).useToPoint(0L).build())
                    .build();
            PaymentStepHandler mockHandler = mock(PaymentStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.proceedTo(SAGA_ID, SagaStep.PAYMENT)).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.PAYMENT))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PRODUCT, ORDER_NO,
                    true, null, null);
            //when
            sagaManager.handleStepResult(command);
            //then
            // 결제 대기로 상태 변경 확인
            verify(sagaService).proceedTo(SAGA_ID, SagaStep.PAYMENT);
            verify(mockHandler).process(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }

    @Nested
    @DisplayName("재고 감소 실패")
    class FailProductStockDeduct {

        @Test
        @DisplayName("재고감소가 실패한 경우 saga를 실패 처리한다")
        void processProductResult_inventory_fail(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PRODUCT)
                    .build();
            SagaInstanceDto failSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.FAILED)
                    .sagaStep(SagaStep.PRODUCT)
                    .build();
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.fail(anyLong(), anyString())).willReturn(failSagaInstance);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PRODUCT, ORDER_NO,
                    false, "SUFFICIENT_STOCK", "재고가 부족합니다");
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).fail(SAGA_ID, "재고가 부족합니다");
            verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());

            assertThat(sagaAbortCaptor.getValue())
                    .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderNo, SagaAbortEvent::getUserId, SagaAbortEvent::getFailureCode)
                    .containsExactly(SAGA_ID, ORDER_NO, USER_ID, "SUFFICIENT_STOCK");
        }
    }

    @Nested
    @DisplayName("재고 복구 성공")
    class SuccessProductStockRestore {

        @Test
        @DisplayName("상품 재고 보상이 완료되면 Saga를 마친다")
        void compensateSaga_inventory_success(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.PRODUCT)
                    .build();
            SagaInstanceDto failSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.FAILED)
                    .sagaStep(SagaStep.PRODUCT)
                    .build();
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.fail(SAGA_ID, null)).willReturn(failSagaInstance);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PRODUCT, ORDER_NO,
                    true, null, null);

            //when
            sagaManager.handleStepResult(command);
            //then
            //사가가 완료됨
            verify(sagaService).fail(SAGA_ID, null);
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 성공")
    class SuccessCouponUsed {

        @Test
        @DisplayName("쿠폰 사용 성공 후 포인트를 사용한다면 포인트단계를 진행한다")
        void proceedSaga_coupon_success_with_point(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.COUPON)
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.USER)
                    .build();
            UserStepHandler mockHandler = mock(UserStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.proceedTo(SAGA_ID, SagaStep.USER)).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.USER))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.COUPON, ORDER_NO,
                    true, null, null);
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).proceedTo(SAGA_ID, SagaStep.USER);
            verify(mockHandler).process(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }

        @Test
        @DisplayName("쿠폰 사용 성공 후 포인트를 사용하지 않는다면 포인트단계를 건너뛰고 결제 대기 단계를 진행한다")
        void proceedSaga_coupon_success_skip_point(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.COUPON)
                    .payload(anPayload().useToPoint(0L).build())
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PAYMENT)
                    .payload(anPayload().useToPoint(0L).build())
                    .build();
            PaymentStepHandler mockHandler = mock(PaymentStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.proceedTo(SAGA_ID, SagaStep.PAYMENT)).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.PAYMENT))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.COUPON, ORDER_NO,
                    true, null, null);
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).proceedTo(SAGA_ID, SagaStep.PAYMENT);
            verify(mockHandler).process(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 실패")
    class FailCouponUsed {

        @Test
        @DisplayName("쿠폰 사용이 실패한 경우 상품 보상을 진행한다")
        void processCouponResult_coupon_fail(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.COUPON)
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.PRODUCT)
                    .build();
            ProductStepHandler mockHandler = mock(ProductStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.startCompensation(SAGA_ID, SagaStep.PRODUCT, "유효하지 않은 쿠폰입니다"))
                    .willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.PRODUCT))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.COUPON, ORDER_NO,
                    false, "INVALID_COUPON", "유효하지 않은 쿠폰입니다");
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).startCompensation(SAGA_ID, SagaStep.PRODUCT, "유효하지 않은 쿠폰입니다");
            verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
            assertThat(sagaAbortCaptor.getValue())
                    .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderNo, SagaAbortEvent::getUserId, SagaAbortEvent::getFailureCode)
                    .containsExactly(SAGA_ID, ORDER_NO, USER_ID, "INVALID_COUPON");

            verify(mockHandler).compensate(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }

    @Nested
    @DisplayName("쿠폰 보상 성공")
    class CouponCompensateSuccess {

        @Test
        @DisplayName("쿠폰 보상이 완료된 이후 상품 재고 보상을 진행한다")
        void compensateSaga_coupon_success(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.COUPON)
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.PRODUCT)
                    .build();
            ProductStepHandler mockHandler = mock(ProductStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.continueCompensation(SAGA_ID, SagaStep.PRODUCT))
                    .willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.PRODUCT))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.COUPON, ORDER_NO,
                    true, null, null);

            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).continueCompensation(SAGA_ID, SagaStep.PRODUCT);
            verify(mockHandler).compensate(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class));
        }

    }

    @Nested
    @DisplayName("포인트 차감 성공")
    class SuccessPointUsed {

        @Test
        @DisplayName("포인트 차감 성공후 결제 대기 상태로 변경한다")
        void proceedSaga_point_success(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.USER)
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PAYMENT)
                    .build();
            PaymentStepHandler mockHandler = mock(PaymentStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.proceedTo(SAGA_ID, SagaStep.PAYMENT)).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.PAYMENT))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.USER, ORDER_NO,
                    true, null, null);
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).proceedTo(SAGA_ID, SagaStep.PAYMENT);
            verify(mockHandler).process(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }

    @Nested
    @DisplayName("포인트 차감 실패")
    class FailPointUsed {

        @Test
        @DisplayName("포인트 차감이 실패한 경우 쿠폰을 사용했다면 쿠폰 보상을 진행한다")
        void processUserResult_point_fail_with_couponId() {
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.USER)
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.COUPON)
                    .build();
            CouponStepHandler mockHandler = mock(CouponStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.startCompensation(SAGA_ID, SagaStep.COUPON, "포인트가 부족합니다")).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.COUPON))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.USER, ORDER_NO,
                    false, "INSUFFICIENT_POINT", "포인트가 부족합니다");
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).startCompensation(SAGA_ID, SagaStep.COUPON, "포인트가 부족합니다");
            verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
            assertThat(sagaAbortCaptor.getValue())
                    .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderNo, SagaAbortEvent::getUserId,
                            SagaAbortEvent::getFailureCode)
                    .containsExactly(SAGA_ID, ORDER_NO, USER_ID, "INSUFFICIENT_POINT");

            verify(mockHandler).compensate(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }

        @Test
        @DisplayName("포인트 차감이 실패한 경우 쿠폰을 사용하지 않았다면 상품 재고 보상을 진행한다")
        void processUserResult_point_fail_without_couponId(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.USER)
                    .payload(anPayload().couponId(null).build())
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.PRODUCT)
                    .payload(anPayload().couponId(null).build())
                    .build();
            ProductStepHandler mockHandler = mock(ProductStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.startCompensation(SAGA_ID, SagaStep.PRODUCT, "포인트가 부족합니다")).willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.PRODUCT))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.USER, ORDER_NO,
                    false, "INSUFFICIENT_POINT", "포인트가 부족합니다");
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).startCompensation(SAGA_ID, SagaStep.PRODUCT, "포인트가 부족합니다");
            verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
            assertThat(sagaAbortCaptor.getValue())
                    .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderNo, SagaAbortEvent::getUserId,
                            SagaAbortEvent::getFailureCode)
                    .containsExactly(SAGA_ID, ORDER_NO, USER_ID, "INSUFFICIENT_POINT");

            verify(mockHandler).compensate(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }

    @Nested
    @DisplayName("SAGA 타임아웃시")
    class TimeOutSaga {

        @Test
        @DisplayName("타임아웃시 타임아웃된 SAGA를 조회하고 보상을 수행한다")
        void processTimeouts(){
            //given
            SagaInstanceDto saga1 = anSagaInstanceDto().id(1L).sagaStatus(SagaStatus.STARTED).sagaStep(SagaStep.COUPON).build();
            SagaInstanceDto saga1_update = anSagaInstanceDto().id(1L).sagaStatus(SagaStatus.COMPENSATING).sagaStep(SagaStep.PRODUCT).build();
            SagaInstanceDto saga2 = anSagaInstanceDto().id(2L).sagaStatus(SagaStatus.STARTED).sagaStep(SagaStep.COUPON).build();
            SagaInstanceDto saga2_update = anSagaInstanceDto().id(2L).sagaStatus(SagaStatus.COMPENSATING).sagaStep(SagaStep.PRODUCT).build();
            ProductStepHandler mockHandler = mock(ProductStepHandler.class);
            given(sagaService.getTimeouts(any(LocalDateTime.class)))
                    .willReturn(List.of(saga1, saga2));
            given(sagaService.startCompensation(saga1.getId(), SagaStep.PRODUCT, "사가 처리 지연"))
                    .willReturn(saga1_update);
            given(sagaService.startCompensation(saga2.getId(), SagaStep.PRODUCT, "사가 처리 지연"))
                    .willReturn(saga2_update);
            given(handlerFactory.getHandler(SagaStep.PRODUCT))
                    .willReturn(mockHandler);
            //when
            sagaManager.processTimeouts();
            //then
            verify(sagaService).startCompensation(saga1.getId(), SagaStep.PRODUCT, "사가 처리 지연");
            verify(sagaService).startCompensation(saga2.getId(), SagaStep.PRODUCT, "사가 처리 지연");

            verify(mockHandler).compensate(
                    eq(saga1.getId()),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
            verify(mockHandler).compensate(
                    eq(saga2.getId()),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }

    @Nested
    @DisplayName("결제 승인 성공시")
    class PaymentSuccess {

        @Test
        @DisplayName("결제 승인 완료시 Saga 종료")
        void processPaymentResult(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PAYMENT)
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.FINISHED)
                    .sagaStep(SagaStep.PAYMENT)
                    .build();
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.finish(SAGA_ID)).willReturn(updateSagaInstance);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, ORDER_NO,
                    true, null, null);
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService, times(1)).finish(SAGA_ID);
        }
    }

    @Nested
    @DisplayName("결제 승인 실패시")
    class PaymentFail {

        @Test
        @DisplayName("결제 승인실패시 포인트를 사용한 경우 포인트 보상을 진행한다")
        void processPaymentResult_paymentFail_with_usedPoint(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PAYMENT)
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.USER)
                    .build();
            UserStepHandler mockHandler = mock(UserStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.startCompensation(SAGA_ID, SagaStep.USER, "잔액이 부족합니다"))
                    .willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.USER))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, ORDER_NO,
                    false, "PAYMENT_INSUFFICIENT_BALANCE", "잔액이 부족합니다");
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).startCompensation(SAGA_ID, SagaStep.USER, "잔액이 부족합니다");
            verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
            assertThat(sagaAbortCaptor.getValue())
                    .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderNo, SagaAbortEvent::getUserId,
                            SagaAbortEvent::getFailureCode)
                    .containsExactly(SAGA_ID, ORDER_NO, USER_ID, "PAYMENT_INSUFFICIENT_BALANCE");

            verify(mockHandler).compensate(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }

        @Test
        @DisplayName("결제 승인 실패시 포인트를 사용하지 않고 쿠폰을 사용했다면 포인트 보상을 건너뛰고 쿠폰 보상을 진행한다")
        void processPaymentResult_paymentFail_skip_user(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PAYMENT)
                    .payload(anPayload().useToPoint(0L).build())
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.COUPON)
                    .payload(anPayload().useToPoint(0L).build())
                    .build();
            CouponStepHandler mockHandler = mock(CouponStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.startCompensation(SAGA_ID, SagaStep.COUPON, "잔액이 부족합니다"))
                    .willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.COUPON))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, ORDER_NO,
                    false, "PAYMENT_INSUFFICIENT_BALANCE", "잔액이 부족합니다");
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).startCompensation(SAGA_ID, SagaStep.COUPON, "잔액이 부족합니다");
            verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
            assertThat(sagaAbortCaptor.getValue())
                    .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderNo, SagaAbortEvent::getUserId,
                            SagaAbortEvent::getFailureCode)
                    .containsExactly(SAGA_ID, ORDER_NO, USER_ID, "PAYMENT_INSUFFICIENT_BALANCE");

            verify(mockHandler).compensate(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }

        @Test
        @DisplayName("결제 승인 실패시 포인트를 사용하지 않고 쿠폰도 사용하지 않았다면 포인트 보상과 쿠폰 보상을 건너뛰고 상품 보상을 진행한다")
        void processPaymentResult_paymentFail_skip_user_and_coupon(){
            //given
            SagaInstanceDto getSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.STARTED)
                    .sagaStep(SagaStep.PAYMENT)
                    .payload(anPayload().useToPoint(0L).couponId(null).build())
                    .build();
            SagaInstanceDto updateSagaInstance = anSagaInstanceDto()
                    .sagaStatus(SagaStatus.COMPENSATING)
                    .sagaStep(SagaStep.PRODUCT)
                    .payload(anPayload().useToPoint(0L).couponId(null).build())
                    .build();
            ProductStepHandler mockHandler = mock(ProductStepHandler.class);
            given(sagaService.getSagaByOrderNo(ORDER_NO)).willReturn(getSagaInstance);
            given(sagaService.startCompensation(SAGA_ID, SagaStep.PRODUCT, "잔액이 부족합니다"))
                    .willReturn(updateSagaInstance);
            given(handlerFactory.getHandler(SagaStep.PRODUCT))
                    .willReturn(mockHandler);
            SagaStepResultCommand command = SagaStepResultCommand.of(SagaStep.PAYMENT, ORDER_NO,
                    false, "PAYMENT_INSUFFICIENT_BALANCE", "잔액이 부족합니다");
            //when
            sagaManager.handleStepResult(command);
            //then
            verify(sagaService).startCompensation(SAGA_ID, SagaStep.PRODUCT, "잔액이 부족합니다");
            verify(eventPublisher).publishEvent(sagaAbortCaptor.capture());
            assertThat(sagaAbortCaptor.getValue())
                    .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderNo, SagaAbortEvent::getUserId,
                            SagaAbortEvent::getFailureCode)
                    .containsExactly(SAGA_ID, ORDER_NO, USER_ID, "PAYMENT_INSUFFICIENT_BALANCE");

            verify(mockHandler).compensate(
                    eq(SAGA_ID),
                    eq(ORDER_NO),
                    any(Payload.class)
            );
        }
    }
}
