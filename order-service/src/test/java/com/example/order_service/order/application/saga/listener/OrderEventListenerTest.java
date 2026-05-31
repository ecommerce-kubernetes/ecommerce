package com.example.order_service.order.application.saga.listener;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.event.OrderCreatedEvent;
import com.example.order_service.order.application.saga.dto.SagaCommand;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.event.PaymentCompletedEvent;
import com.example.order_service.order.application.event.PaymentFailedEvent;
import com.example.order_service.order.domain.model.OrderFailureCode;
import com.example.order_service.order.application.saga.domain.model.SagaStep;
import com.example.order_service.order.application.saga.orchestrator.SagaManager;
import com.example.order_service.order.application.saga.orchestrator.dto.command.SagaStepResultCommand;
import com.example.order_service.order.application.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.order.application.saga.orchestrator.event.SagaResourceSecuredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderEventListenerTest {

    @InjectMocks
    private OrderEventListener orderEventListener;
    @Mock
    private SagaManager sagaManager;
    @Mock
    private OrderFacade orderFacade;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";
    @Captor
    private ArgumentCaptor<SagaStepResultCommand> sagaStepResultCaptor;


    @Test
    @DisplayName("주문 생성 이벤트를 수신하면 SAGA 를 수행한다")
    void handleOrderCreated() {
        //given
        OrderCreatedEvent orderEvent = createOrderEvent();
        //when
        orderEventListener.handleOrderCreated(orderEvent);
        //then
        ArgumentCaptor<SagaCommand.StartSaga> captor =
                ArgumentCaptor.forClass(SagaCommand.StartSaga.class);
        then(sagaManager).should().startSaga(captor.capture());

        SagaCommand.StartSaga command = captor.getValue();
        assertThat(command.orderNo()).isEqualTo(orderEvent.getOrderNo());
        assertThat(command.pointDeduction().userId()).isEqualTo(orderEvent.getUserId());
        assertThat(command.stockDeductions()).hasSize(orderEvent.getOrderedItems().size());
    }

    private OrderCreatedEvent createOrderEvent() {
        OrderCreatedEvent.OrderedItem item = OrderCreatedEvent.OrderedItem.builder()
                .productVariantId(1L)
                .quantity(1)
                .itemCouponId(2L)
                .build();
        return OrderCreatedEvent.builder()
                .orderNo("orderNo")
                .userId(1L)
                .cartCouponId(1L)
                .orderedItems(List.of(item))
                .usedPoint(Money.wons(1000L))
                .build();
    }

    @Test
    @DisplayName("Saga가 결제 대기 상태가 되면 주문의 상태를 변경하기 위해 orderApplicationService를 호출한다")
    void handleSagaCompleted() {
        //given
        SagaResourceSecuredEvent sagaResourceSecuredEvent = SagaResourceSecuredEvent.of(1L, ORDER_NO, 1L);
        //when
        orderEventListener.handleSagaCompleted(sagaResourceSecuredEvent);
        //then
        verify(orderFacade, times(1)).preparePayment(ORDER_NO);
    }

    @Nested
    @DisplayName("결제 이벤트 수신시")
    class PaymentEvent {

        @Test
        @DisplayName("결제 성공 이벤트 수신시 Saga를 완료한다")
        void handlePaymentCompleted(){
            //given
            PaymentCompletedEvent event = PaymentCompletedEvent.of(ORDER_NO, 1L, List.of(1L, 2L));
            //when
            orderEventListener.handlePaymentCompleted(event);
            //then
            verify(sagaManager).handleStepResult(sagaStepResultCaptor.capture());
            assertThat(sagaStepResultCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo, SagaStepResultCommand::isSuccess,
                            SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.PAYMENT, ORDER_NO, true, null, null);
        }

        @Test
        @DisplayName("결제 실패 이벤트 수신시 Saga를 실패처리하고 보상로직을 실행한다")
        void handlePaymentFailed(){
            //given
            PaymentFailedEvent event = PaymentFailedEvent.of(ORDER_NO, 1L, "PAYMENT_INSUFFICIENT_BALANCE", "잔액이 부족합니다");
            //when
            orderEventListener.handlePaymentFailed(event);
            //then
            verify(sagaManager).handleStepResult(sagaStepResultCaptor.capture());
            assertThat(sagaStepResultCaptor.getValue())
                    .extracting(SagaStepResultCommand::getStep, SagaStepResultCommand::getOrderNo, SagaStepResultCommand::isSuccess,
                            SagaStepResultCommand::getErrorCode, SagaStepResultCommand::getFailureReason)
                    .containsExactly(SagaStep.PAYMENT, ORDER_NO, false, "PAYMENT_INSUFFICIENT_BALANCE", "잔액이 부족합니다");
        }
    }

    @Nested
    @DisplayName("SAGA 실패시")
    class SagaAborted {

        @Test
        @DisplayName("Saga 포인트 부족 이벤트 수신 시 INSUFFICIENT_POINT 코드로 실패 처리한다")
        void handleSagaAbort_publish_insufficient_point_event(){
            //given
            SagaAbortEvent abortEvent = SagaAbortEvent.of(1L, ORDER_NO, 1L, "INSUFFICIENT_POINT");
            //when
            orderEventListener.handleSagaAborted(abortEvent);
            //then
            verify(orderFacade).processOrderFailure(ORDER_NO, OrderFailureCode.INSUFFICIENT_POINT);
        }

        @Test
        @DisplayName("Saga 유효하지 않은 쿠폰 이벤트 수신시 INVALID_COUPON 코드로 실패 처리한다")
        void handleSagaAbort_publish_invalid_coupon(){
            //given
            SagaAbortEvent abortEvent = SagaAbortEvent.of(1L, ORDER_NO, 1L, "INVALID_COUPON");
            //when
            orderEventListener.handleSagaAborted(abortEvent);
            //then
            verify(orderFacade).processOrderFailure(ORDER_NO, OrderFailureCode.INVALID_COUPON);
        }

        @Test
        @DisplayName("Saga 만료된 쿠폰 이벤트 수신시 COUPON_EXPIRED 코드로 실패 처리한다")
        void handleSagaAbort_publish_coupon_expired(){
            //given
            SagaAbortEvent abortEvent = SagaAbortEvent.of(1L, ORDER_NO, 1L, "COUPON_EXPIRED");
            //when
            orderEventListener.handleSagaAborted(abortEvent);
            //then
            verify(orderFacade).processOrderFailure(ORDER_NO, OrderFailureCode.COUPON_EXPIRED);
        }

        @Test
        @DisplayName("Saga 재고 부족 이벤트 수신시 코드로 실패 처리한다")
        void handleSagaAbort_publish_insufficient_stock(){
            //given
            SagaAbortEvent abortEvent = SagaAbortEvent.of(1L, ORDER_NO, 1L, "INSUFFICIENT_STOCK");
            //when
            orderEventListener.handleSagaAborted(abortEvent);
            //then
            verify(orderFacade).processOrderFailure(ORDER_NO, OrderFailureCode.INSUFFICIENT_STOCK);
        }
    }

}
