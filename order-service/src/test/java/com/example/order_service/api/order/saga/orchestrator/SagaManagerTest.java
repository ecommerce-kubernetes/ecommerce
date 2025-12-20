package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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

    @Test
    @DisplayName("Saga 시작 시 인스턴스를 저장하고 재고 차감 요청 이벤트를 발행한다")
    void startSaga() {
        //given
        Long sagaId = 1L;
        SagaStartCommand.DeductProduct deductProduct1 = SagaStartCommand.DeductProduct.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        SagaStartCommand.DeductProduct deductProduct2 = SagaStartCommand.DeductProduct.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();

        SagaStartCommand command = SagaStartCommand.builder()
                .orderId(1L)
                .userId(1L)
                .couponId(1L)
                .deductProductList(List.of(deductProduct1, deductProduct2))
                .usedPoint(1000L)
                .build();

        SagaInstanceDto sagaInstanceDto = createSagaInstanceDto(sagaId, 1L, SagaStatus.STARTED, SagaStep.PRODUCT,
                Payload.from(command));

        given(orderSagaDomainService.create(anyLong(), any(Payload.class)))
                .willReturn(sagaInstanceDto);

        //when
        sagaManager.startSaga(command);
        //then
        ArgumentCaptor<Long> orderIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Payload> payloadCaptor = ArgumentCaptor.forClass(Payload.class);
        verify(orderSagaDomainService, times(1))
                .create(orderIdCaptor.capture(), payloadCaptor.capture());

        assertThat(orderIdCaptor.getValue()).isEqualTo(1L);
        assertThat(payloadCaptor.getValue())
                .extracting("userId", "couponId", "useToPoint")
                .containsExactly(1L, 1L, 1000L);

        assertThat(payloadCaptor.getValue().getSagaItems())
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3),
                        tuple(2L, 5)
                );

        verify(sagaEventProducer).requestInventoryDeduction(
                eq(sagaId),
                eq(sagaInstanceDto.getId()),
                refEq(sagaInstanceDto.getPayload())
        );
    }

    @Test
    @DisplayName("재고감소 성공 후 쿠폰과 포인트를 모두 사용한다면 다음 단계인 쿠폰 단계를 실행한다")
    void proceedSaga_inventory_success_with_coupon_point(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long userId = 1L;
        Long couponId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();
        SagaInstanceDto initInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.PRODUCT, payload);
        SagaInstanceDto couponInstanceDto = createSagaInstanceDto(sagaId, orderId,SagaStatus.STARTED, SagaStep.COUPON, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(initInstanceDto);
        given(orderSagaDomainService.proceedTo(anyLong(), any(SagaStep.class)))
                .willReturn(couponInstanceDto);
        //when
        sagaManager.proceedSaga(sagaId);
        //then

        // 쿠폰 관련 로직은 실행되어야 함
        verify(orderSagaDomainService, times(1)).proceedTo(sagaId, SagaStep.COUPON);
        verify(sagaEventProducer, times(1)).requestCouponUse(sagaId, orderId, payload);

        // 다음 단계의 로직은 실행되서는 안됨
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.USER);
        verify(orderSagaDomainService, never()).finish(sagaId);

        verify(sagaEventProducer, never()).requestUserPointUse(sagaId, orderId, payload);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("재고감소 성공 후 쿠폰은 사용하지 않고 포인트만 사용한다면 다음 단계인 쿠폰 단계를 건너뛰고 포인트 단계를 실행한다")
    void proceedSaga_inventory_success_skip_coupon(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(null)
                .useToPoint(usedPoint)
                .build();
        SagaInstanceDto initInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.PRODUCT, payload);
        SagaInstanceDto pointInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.USER, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(initInstanceDto);
        given(orderSagaDomainService.proceedTo(anyLong(), any(SagaStep.class)))
                .willReturn(pointInstanceDto);
        //when
        sagaManager.proceedSaga(sagaId);
        //then

        //포인트 관련 로직은 실행되어야함
        verify(orderSagaDomainService, times(1)).proceedTo(sagaId, SagaStep.USER);
        verify(sagaEventProducer, times(1)).requestUserPointUse(sagaId, orderId, payload);

        // 쿠폰단계 또는 사가 완료 단계는 실행되서는 안됨
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.COUPON);
        verify(orderSagaDomainService, never()).finish(sagaId);

        verify(sagaEventProducer, never()).requestCouponUse(sagaId, orderId, payload);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("재고감소 성공 후 쿠폰과 포인트 모두 사용하지 않는다면 쿠폰 단계와 포인트 단계를 건너뛰고 사가 완료 단계를 실행한다")
    void proceedSaga_inventory_success_all_skip(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long userId = 1L;
        Long usedPoint = 0L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(null)
                .useToPoint(usedPoint)
                .build();
        SagaInstanceDto initInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.PRODUCT, payload);
        SagaInstanceDto pointInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.FINISHED, SagaStep.PRODUCT, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(initInstanceDto);
        given(orderSagaDomainService.finish(anyLong()))
                .willReturn(pointInstanceDto);
        //when
        sagaManager.proceedSaga(sagaId);
        //then
        // 사가 완료 단계가 실행되어야 함
        ArgumentCaptor<SagaCompletedEvent> sagaCompleteCaptor = ArgumentCaptor.forClass(SagaCompletedEvent.class);
        verify(orderSagaDomainService, times(1)).finish(sagaId);
        verify(eventPublisher, times(1)).publishEvent(sagaCompleteCaptor.capture());
        assertThat(sagaCompleteCaptor.getValue())
                .extracting(SagaCompletedEvent::getSagaId, SagaCompletedEvent::getOrderId, SagaCompletedEvent::getUserId)
                        .containsExactly(1L, 1L, 1L);

        // 쿠폰단계 또는 포인트 단계가 실행되서는 안됨
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.COUPON);
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.USER);

        verify(sagaEventProducer, never()).requestCouponUse(sagaId, orderId, payload);
        verify(sagaEventProducer, never()).requestUserPointUse(sagaId, orderId, payload);
    }

    @Test
    @DisplayName("쿠폰 사용 성공 후 포인트를 사용한다면 포인트단계를 진행한다")
    void proceedSaga_coupon_success_with_point(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long couponId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();
        SagaInstanceDto initInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.COUPON, payload);
        SagaInstanceDto pointInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.USER, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(initInstanceDto);
        given(orderSagaDomainService.proceedTo(anyLong(), any(SagaStep.class)))
                .willReturn(pointInstanceDto);
        //when
        sagaManager.proceedSaga(sagaId);
        //then
        // 포인트 단계가 실행되어야 함
        verify(orderSagaDomainService, times(1)).proceedTo(sagaId, SagaStep.USER);
        verify(sagaEventProducer, times(1)).requestUserPointUse(sagaId, orderId, payload);

        // 쿠폰단계 또는 사가 완료 단계는 실행되서는 안됨
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.COUPON);
        verify(orderSagaDomainService, never()).finish(sagaId);

        verify(sagaEventProducer, never()).requestCouponUse(sagaId, orderId, payload);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("쿠폰 사용 성공 후 포인트를 사용하지 않는다면 포인트단계를 건너뛰고 사가 완료 단계를 진행한다")
    void proceedSaga_coupon_success_skip_point(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long couponId = 1L;
        Long userId = 1L;
        Long usedPoint = 0L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();
        SagaInstanceDto initInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.COUPON, payload);
        SagaInstanceDto completeInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.FINISHED, SagaStep.COUPON, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(initInstanceDto);
        given(orderSagaDomainService.finish(anyLong()))
                .willReturn(completeInstanceDto);
        //when
        sagaManager.proceedSaga(sagaId);
        //then
        ArgumentCaptor<SagaCompletedEvent> sagaCompleteCaptor = ArgumentCaptor.forClass(SagaCompletedEvent.class);
        verify(orderSagaDomainService, times(1)).finish(sagaId);
        verify(eventPublisher, times(1)).publishEvent(sagaCompleteCaptor.capture());
        assertThat(sagaCompleteCaptor.getValue())
                .extracting(SagaCompletedEvent::getSagaId, SagaCompletedEvent::getOrderId, SagaCompletedEvent::getUserId)
                .containsExactly(1L, 1L, 1L);

        // 쿠폰단계 또는 포인트 단계가 실행되서는 안됨
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.COUPON);
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.USER);

        verify(sagaEventProducer, never()).requestCouponUse(sagaId, orderId, payload);
        verify(sagaEventProducer, never()).requestUserPointUse(sagaId, orderId, payload);
    }

    @Test
    @DisplayName("포인트 차감 성공후 사가 완료 단계를 진행한다")
    void proceedSaga_point_success(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long couponId = 1L;
        Long userId = 1L;
        Long usedPoint = 0L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();
        SagaInstanceDto initInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.STARTED, SagaStep.USER, payload);
        SagaInstanceDto completeInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.FINISHED, SagaStep.USER, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(initInstanceDto);
        given(orderSagaDomainService.finish(anyLong()))
                .willReturn(completeInstanceDto);
        //when
        sagaManager.proceedSaga(sagaId);
        //then
        ArgumentCaptor<SagaCompletedEvent> sagaCompleteCaptor = ArgumentCaptor.forClass(SagaCompletedEvent.class);
        verify(orderSagaDomainService, times(1)).finish(sagaId);
        verify(eventPublisher, times(1)).publishEvent(sagaCompleteCaptor.capture());
        assertThat(sagaCompleteCaptor.getValue())
                .extracting(SagaCompletedEvent::getSagaId, SagaCompletedEvent::getOrderId, SagaCompletedEvent::getUserId)
                .containsExactly(1L, 1L, 1L);

        // 쿠폰단계 또는 포인트 단계가 실행되서는 안됨
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.COUPON);
        verify(orderSagaDomainService, never()).proceedTo(sagaId, SagaStep.USER);

        verify(sagaEventProducer, never()).requestCouponUse(sagaId, orderId, payload);
        verify(sagaEventProducer, never()).requestUserPointUse(sagaId, orderId, payload);
    }

    @Test
    @DisplayName("포인트 차감이 실패한 경우 쿠폰을 사용했다면 쿠폰 보상을 진행한다")
    void abortSaga_point_fail_with_couponId() {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long couponId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();

        SagaInstanceDto abortInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.ABORTED, SagaStep.USER, payload);
        SagaInstanceDto compensateInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.COMPENSATING, SagaStep.COUPON, payload);
        given(orderSagaDomainService.abort(anyLong(), anyString()))
                .willReturn(abortInstanceDto);
        given(orderSagaDomainService.compensateTo(anyLong(), any(SagaStep.class)))
                .willReturn(compensateInstanceDto);
        //when
        sagaManager.abortSaga(sagaId,"INSUFFICIENT_POINT", "포인트 부족");
        //then
        //포인트 차감과정 문제 발생이므로 SagaAbort 이벤트 발행과 다음 스텝인 Coupon 보상이 이뤄져야함
        ArgumentCaptor<SagaAbortEvent> captor = ArgumentCaptor.forClass(SagaAbortEvent.class);
        verify(orderSagaDomainService, times(1)).compensateTo(sagaId, SagaStep.COUPON);
        verify(sagaEventProducer, times(1)).requestCouponCompensate(sagaId, orderId, payload);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        assertThat(captor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                        .containsExactly(1L, 1L, 1L, OrderFailureCode.POINT_SHORTAGE);

        //재고 복구 로직은 실행되서는 안됨
        verify(orderSagaDomainService, never()).compensateTo(sagaId, SagaStep.PRODUCT);
        verify(sagaEventProducer, never()).requestInventoryCompensate(sagaId, orderId, payload);
        //SAGA 가 완료되서는 안됨
        verify(orderSagaDomainService, never()).fail(sagaId);
    }

    @Test
    @DisplayName("포인트 차감이 실패한 경우 쿠폰을 사용하지 않았다면 상품 재고 보상을 진행한다")
    void abortSaga_point_fail_without_couponId(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(null)
                .useToPoint(usedPoint)
                .build();

        SagaInstanceDto abortInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.ABORTED, SagaStep.USER, payload);
        SagaInstanceDto compensateInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.COMPENSATING, SagaStep.PRODUCT, payload);
        given(orderSagaDomainService.abort(anyLong(), anyString()))
                .willReturn(abortInstanceDto);
        given(orderSagaDomainService.compensateTo(anyLong(), any(SagaStep.class)))
                .willReturn(compensateInstanceDto);
        //when
        sagaManager.abortSaga(sagaId,"INSUFFICIENT_POINT", "포인트 부족");
        //then
        //쿠폰을 사용하지 않았으므로 쿠폰 보상을 건너 뛰고 상품 재고 보상이 진행되어야 함
        ArgumentCaptor<SagaAbortEvent> captor = ArgumentCaptor.forClass(SagaAbortEvent.class);
        verify(orderSagaDomainService, times(1)).compensateTo(sagaId, SagaStep.PRODUCT);
        verify(sagaEventProducer, times(1)).requestInventoryCompensate(sagaId, orderId, payload);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        assertThat(captor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                .containsExactly(1L, 1L, 1L, OrderFailureCode.POINT_SHORTAGE);

        //쿠폰 복구는 진행되서는 안됨
        verify(orderSagaDomainService, never()).compensateTo(sagaId, SagaStep.COUPON);
        verify(sagaEventProducer, never()).requestCouponCompensate(sagaId, orderId, payload);
        //SAGA 가 완료되서는 안됨
        verify(orderSagaDomainService, never()).fail(sagaId);
    }

    @Test
    @DisplayName("쿠폰 사용이 실패한 경우 상품 보상을 진행한다")
    void abortSaga_coupon_fail(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(null)
                .useToPoint(usedPoint)
                .build();


        SagaInstanceDto abortInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.ABORTED, SagaStep.COUPON, payload);
        SagaInstanceDto compensateInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.COMPENSATING, SagaStep.PRODUCT, payload);
        given(orderSagaDomainService.abort(anyLong(), anyString()))
                .willReturn(abortInstanceDto);
        given(orderSagaDomainService.compensateTo(anyLong(), any(SagaStep.class)))
                .willReturn(compensateInstanceDto);
        //when
        sagaManager.abortSaga(sagaId,"INVALID_COUPON", "유효하지 않은 쿠폰");
        //then
        ArgumentCaptor<SagaAbortEvent> captor = ArgumentCaptor.forClass(SagaAbortEvent.class);
        verify(orderSagaDomainService, times(1)).compensateTo(sagaId, SagaStep.PRODUCT);
        verify(sagaEventProducer, times(1)).requestInventoryCompensate(sagaId, orderId, payload);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        assertThat(captor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                .containsExactly(1L, 1L, 1L, OrderFailureCode.INVALID_COUPON);

        //SAGA 가 완료되서는 안됨
        verify(orderSagaDomainService, never()).fail(sagaId);
    }

    @Test
    @DisplayName("재고감소가 실패한 경우 saga를 실패 처리한다")
    void abortSaga_inventory_fail(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(null)
                .useToPoint(usedPoint)
                .build();

        SagaInstanceDto abortInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.ABORTED, SagaStep.PRODUCT, payload);
        SagaInstanceDto failInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.FAILED, SagaStep.PRODUCT, payload);
        given(orderSagaDomainService.abort(anyLong(), anyString()))
                .willReturn(abortInstanceDto);
        given(orderSagaDomainService.fail(anyLong()))
                .willReturn(failInstanceDto);
        //when
        sagaManager.abortSaga(sagaId, "OUT_OF_STOCK", "재고가 부족합니다");
        //then
        ArgumentCaptor<SagaAbortEvent> captor = ArgumentCaptor.forClass(SagaAbortEvent.class);
        verify(orderSagaDomainService, times(1)).fail(sagaId);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        verify(sagaEventProducer, never()).requestInventoryCompensate(sagaId, orderId, payload);

        assertThat(captor.getValue())
                .extracting(SagaAbortEvent::getSagaId, SagaAbortEvent::getOrderId, SagaAbortEvent::getUserId,
                        SagaAbortEvent::getOrderFailureCode)
                .containsExactly(1L, 1L, 1L, OrderFailureCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("쿠폰 보상이 완료된 이후 상품 재고 보상을 진행한다")
    void compensateSaga_coupon_success(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long couponId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();

        SagaInstanceDto currentInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.COMPENSATING, SagaStep.COUPON, payload);
        SagaInstanceDto compensateInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.COMPENSATING, SagaStep.PRODUCT, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(currentInstanceDto);
        given(orderSagaDomainService.compensateTo(anyLong(), any(SagaStep.class)))
                .willReturn(compensateInstanceDto);
        //when
        sagaManager.compensateSaga(sagaId);
        //then
        verify(orderSagaDomainService, times(1)).compensateTo(sagaId, SagaStep.PRODUCT);
        verify(sagaEventProducer, times(1)).requestInventoryCompensate(sagaId, orderId, payload);

        //쿠폰 복구는 진행되서는 안됨
        verify(orderSagaDomainService, never()).compensateTo(sagaId, SagaStep.COUPON);
        verify(sagaEventProducer, never()).requestCouponCompensate(sagaId, orderId, payload);
        //SAGA 가 완료되서는 안됨, Saga 실패 이벤트가 발행되서는 안됨
        verify(orderSagaDomainService, never()).fail(sagaId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("상품 재고 보상이 완료되면 Saga를 마친다")
    void compensateSaga_inventory_success(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long couponId = 1L;
        Long userId = 1L;
        Long usedPoint = 1000L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();

        SagaInstanceDto currentInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.COMPENSATING, SagaStep.PRODUCT, payload);
        SagaInstanceDto failInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStatus.FAILED, SagaStep.PRODUCT, payload);
        given(orderSagaDomainService.getSaga(anyLong()))
                .willReturn(currentInstanceDto);
        given(orderSagaDomainService.fail(anyLong()))
                .willReturn(failInstanceDto);
        //when
        sagaManager.compensateSaga(sagaId);
        //then
        //사가가 완료됨
        verify(orderSagaDomainService, times(1)).fail(sagaId);

        //다른 단계는 진행되서는 안됨
        verify(sagaEventProducer, never()).requestInventoryCompensate(sagaId, orderId, payload);
        verify(sagaEventProducer, never()).requestCouponCompensate(sagaId, orderId, payload);
        verify(orderSagaDomainService, never()).compensateTo(sagaId, SagaStep.COUPON);
        verify(orderSagaDomainService, never()).compensateTo(sagaId, SagaStep.PRODUCT);
        verify(eventPublisher, never()).publishEvent(any());
    }

    private SagaInstanceDto createSagaInstanceDto(Long sagaId, Long orderId,
                                                  SagaStatus sagaStatus, SagaStep sagaStep,  Payload payload) {
        return SagaInstanceDto.builder()
                .id(sagaId)
                .orderId(orderId)
                .sagaStatus(sagaStatus)
                .sagaStep(sagaStep)
                .payload(payload)
                .build();
    }
}
