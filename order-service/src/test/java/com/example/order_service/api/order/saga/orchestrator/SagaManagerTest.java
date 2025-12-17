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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
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

    //TODO @Test로 시나리오별로 테스트 분리
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInventoryDeductScenarios")
    @DisplayName("재고 감소 성공시 SAGA 시나리오별 검증")
    @SuppressWarnings("ConstantConditions")
    void proceedSaga_inventory_success_scenario(String description, Long couponId, Long usedPoint,
                                                VerificationMode couponStatus, VerificationMode couponEvent,
                                                VerificationMode pointStatus, VerificationMode pointEvent,
                                                VerificationMode completeStatus, boolean completeEvent) {
        //given
        Long sagaId = 1L;
        Long orderId = 1L;
        Long userId = 1L;
        Payload payload = Payload.builder()
                .userId(userId)
                .sagaItems(List.of(Payload.SagaItem.builder().productVariantId(1L).quantity(3).build()))
                .couponId(couponId)
                .useToPoint(usedPoint)
                .build();

        SagaInstanceDto initInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStep.PRODUCT, SagaProgress.STARTED, payload);
        SagaInstanceDto couponInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStep.COUPON, SagaProgress.STARTED, payload);
        SagaInstanceDto userInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStep.USER, SagaProgress.STARTED, payload);
        SagaInstanceDto completeInstanceDto = createSagaInstanceDto(sagaId, orderId, SagaStep.USER, SagaProgress.COMPLETED, payload);
        given(orderSagaDomainService.getOrderSagaInstance(anyLong())).willReturn(initInstanceDto);
        lenient().when(orderSagaDomainService.updateToCouponSagaInstance(anyLong())).thenReturn(couponInstanceDto);
        lenient().when(orderSagaDomainService.updateToPointSagaInstance(anyLong())).thenReturn(userInstanceDto);
        lenient().when(orderSagaDomainService.updateToCompleteSagaInstance(anyLong())).thenReturn(completeInstanceDto);
        //when
        sagaManager.proceedSaga(1L);

        //쿠폰 관련 검증
        verify(orderSagaDomainService, couponStatus).updateToCouponSagaInstance(sagaId);
        verify(sagaEventProducer, couponEvent).requestCouponUse(sagaId, orderId, payload);

        // 포인트 관련 검증
        verify(orderSagaDomainService, pointStatus).updateToPointSagaInstance(sagaId);
        verify(sagaEventProducer, pointEvent).requestUserPointUse(sagaId, orderId, payload);

        // Saga 완료 검증
        verify(orderSagaDomainService, completeStatus).updateToCompleteSagaInstance(sagaId);

        if (completeEvent) {
            verify(eventPublisher, times(1)).publishEvent(refEq(SagaCompletedEvent.of(sagaId, orderId, userId)));
        } else {
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    private SagaInstanceDto createSagaInstanceDto(Long sagaId, Long orderId,
                                                  SagaStep sagaStep, SagaProgress sagaProgress, Payload payload) {
        return SagaInstanceDto.builder()
                .id(sagaId)
                .orderId(orderId)
                .sagaStep(sagaStep)
                .sagaProgress(sagaProgress)
                .payload(payload)
                .build();
    }

    private static Stream<Arguments> provideInventoryDeductScenarios() {
        return Stream.of(
                //재고 감소 성공 메시지 수신시 쿠폰 사용, 포인트 사용 시나리오
                Arguments.of(
                        "쿠폰 사용, 포인트 사용",
                        1L, 1000L, // 쿠폰 Id, 사용 포인트
                        times(1), times(1), // 쿠폰상태 변경, 쿠폰 사용 메시지 발송
                        never(), never(), // 포인트 상태 변경, 포인트 사용 메시지 발송
                        never(), false// Saga 완료 상태 변경, Saga 완료 메시지 발행
                ),

                //재고 감소 성공 메시지 수신시 쿠폰 미사용, 포인트 사용 시나리오
                Arguments.of(
                        "쿠폰 미사용, 포인트 사용",
                        null, 1000L, // 쿠폰 Id, 사용 포인트
                        never(), never(), // 쿠폰상태 변경, 쿠폰 사용 메시지 발송
                        times(1), times(1), // 포인트 상태 변경, 포인트 사용 메시지 발송
                        never(), false // Saga 완료 상태 변경, Saga 완료 메시지 발행
                ),

                //재고 감소 성공 메시지 수신시 쿠폰 미사용, 포인트 미사용 시나리오
                Arguments.of(
                        "쿠폰 미사용, 포인트 미사용",
                        null, 0L, // 쿠폰 Id, 사용 포인트
                        never(), never(), // 쿠폰상태 변경, 쿠폰 사용 메시지 발송
                        never(), never(), // 포인트 상태 변경, 포인트 사용 메시지 발송
                        times(1), true // Saga 완료 상태 변경, Saga 완료 메시지 발행
                )
        );
    }
}
