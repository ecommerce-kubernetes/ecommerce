package com.example.order_service.api.order.saga.orchestrator;

import com.example.order_service.api.order.saga.domain.model.SagaProgress;
import com.example.order_service.api.order.saga.domain.model.SagaStep;
import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import com.example.order_service.api.order.saga.domain.service.OrderSagaDomainService;
import com.example.order_service.api.order.saga.domain.service.dto.SagaInstanceDto;
import com.example.order_service.api.order.saga.infrastructure.kafka.producer.SagaEventProducer;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
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

        SagaInstanceDto sagaInstanceDto = createSagaInstanceDto(sagaId, 1L, SagaStep.PRODUCT, SagaProgress.STARTED,
                Payload.from(command));

        given(orderSagaDomainService.saveOrderSagaInstance(anyLong(), any(Payload.class)))
                .willReturn(sagaInstanceDto);

        //when
        sagaManager.startSaga(command);
        //then
        ArgumentCaptor<Long> orderIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Payload> payloadCaptor = ArgumentCaptor.forClass(Payload.class);
        verify(orderSagaDomainService, times(1))
                .saveOrderSagaInstance(orderIdCaptor.capture(), payloadCaptor.capture());

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
    @DisplayName("쿠폰 아이디가 존재하면 상태를 업데이트 하고 쿠폰 사용 이벤트를 발행한다")
    void proceedToCoupon_WithCouponId(){
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long couponId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(1000L)
                .build();

        SagaInstanceDto initSagaInstance = createSagaInstanceDto(sagaId, orderId, SagaStep.PRODUCT, SagaProgress.STARTED, payload);
        SagaInstanceDto updateSagaInstance = createSagaInstanceDto(sagaId, orderId, SagaStep.COUPON, SagaProgress.STARTED, payload);
        given(orderSagaDomainService.getOrderSagaInstance(anyLong()))
                .willReturn(initSagaInstance);
        given(orderSagaDomainService.updateToCouponSagaInstance(sagaId))
                .willReturn(updateSagaInstance);
        //when
        sagaManager.proceedToCoupon(sagaId);
        //then
        verify(sagaEventProducer, times(1))
                .requestCouponUse(updateSagaInstance.getId(), updateSagaInstance.getOrderId(), updateSagaInstance.getPayload());

        verify(orderSagaDomainService, times(1)).updateToCouponSagaInstance(sagaId);
    }

    @Test
    @DisplayName("쿠폰 아이디가 없으면 쿠폰 단계를 건너뛰고 포인트 차감 단계로 넘어간다")
    void proceedToCoupon_WithoutCouponId() {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .useToPoint(1000L)
                .build();
        SagaInstanceDto initSagaInstance = createSagaInstanceDto(sagaId, orderId, SagaStep.PRODUCT, SagaProgress.STARTED, payload);
        given(orderSagaDomainService.getOrderSagaInstance(anyLong()))
                .willReturn(initSagaInstance);
        SagaManager spyManager = spy(new SagaManager(orderSagaDomainService, sagaEventProducer, eventPublisher));
        doNothing().when(spyManager).proceedToUserPoint(anyLong());
        //when
        spyManager.proceedToCoupon(sagaId);
        //then
        verify(spyManager, times(1)).proceedToUserPoint(sagaId);
        verify(sagaEventProducer, never()).requestCouponUse(anyLong(), anyLong(), any(Payload.class));
        verify(orderSagaDomainService, never()).updateToCouponSagaInstance(anyLong());
    }

    @Test
    @DisplayName("사용 포인트가 null 또는 0 이 아닌경우 포인트 사용 상태로 변경하고 포인트 차감 메시지를 발행한다")
    void proceedToUserPoint_WithPoint() {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .useToPoint(1000L)
                .build();

        SagaInstanceDto initSagaInstance = createSagaInstanceDto(sagaId, orderId, SagaStep.COUPON, SagaProgress.STARTED, payload);
        SagaInstanceDto updateSagaInstance = createSagaInstanceDto(sagaId, orderId, SagaStep.USER, SagaProgress.STARTED, payload);
        given(orderSagaDomainService.getOrderSagaInstance(anyLong()))
                .willReturn(initSagaInstance);
        given(orderSagaDomainService.updateToPointSagaInstance(sagaId))
                .willReturn(updateSagaInstance);
        //when
        sagaManager.proceedToUserPoint(sagaId);
        //then
        verify(sagaEventProducer, times(1))
                .requestUserPointUse(updateSagaInstance.getId(), updateSagaInstance.getOrderId(), updateSagaInstance.getPayload());
        verify(orderSagaDomainService, times(1)).updateToPointSagaInstance(sagaId);
    }

    @Test
    @DisplayName("포인트가 null 또는 0인 경우 포인트 사용 단계를 건너뛰고 사가 완료 단계로 넘어간다")
    void proceedToUserPoint_WithoutPoint() {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .useToPoint(0L)
                .build();

        SagaInstanceDto initSagaInstance = createSagaInstanceDto(sagaId, orderId, SagaStep.COUPON, SagaProgress.STARTED, payload);
        given(orderSagaDomainService.getOrderSagaInstance(anyLong()))
                .willReturn(initSagaInstance);
        SagaManager spySagaManager = spy(new SagaManager(orderSagaDomainService, sagaEventProducer, eventPublisher));
        doNothing().when(spySagaManager).completeSaga(anyLong());
        //when
        spySagaManager.proceedToUserPoint(sagaId);
        //then
        verify(spySagaManager, times(1)).completeSaga(sagaId);
        verify(sagaEventProducer, never()).requestUserPointUse(anyLong(), anyLong(), any(Payload.class));
        verify(orderSagaDomainService, never()).updateToPointSagaInstance(anyLong());
    }

    @Test
    @DisplayName("사가가 완료되면 사가 완료 상태로 변경하고 사가 완료 이벤트를 발행한다")
    void completeSaga() {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Payload payload = Payload.builder()
                .userId(1L)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .useToPoint(0L)
                .build();

        SagaInstanceDto sagaInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStep.USER, SagaProgress.COMPLETED, payload);
        given(orderSagaDomainService.updateToCompleteSagaInstance(sagaId))
                .willReturn(sagaInstanceDto);
        //when
        sagaManager.completeSaga(sagaId);
        //then
        ArgumentCaptor<SagaCompletedEvent> captor = ArgumentCaptor.forClass(SagaCompletedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        assertThat(captor.getValue())
                .extracting("sagaId", "orderId", "userId")
                .containsExactly(1L, 1L, 1L);
    }

    private SagaInstanceDto createSagaInstanceDto(Long sagaId, Long orderId,
                                                  SagaStep sagaStep, SagaProgress sagaProgress, Payload payload) {
        return SagaInstanceDto.builder()
                .id(sagaId)
                .orderId(orderId)
                .sagaStep(sagaStep.name())
                .sagaProgress(sagaProgress.name())
                .payload(payload)
                .build();
    }
}
