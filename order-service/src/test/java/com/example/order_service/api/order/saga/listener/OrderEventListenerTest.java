package com.example.order_service.api.order.saga.listener;

import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.facade.OrderFacade;
import com.example.order_service.api.order.facade.event.OrderCreatedEvent;
import com.example.order_service.api.order.facade.event.OrderEventStatus;
import com.example.order_service.api.order.facade.event.PaymentResultEvent;
import com.example.order_service.api.order.saga.orchestrator.SagaManager;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaPaymentCommand;
import com.example.order_service.api.order.saga.orchestrator.dto.command.SagaStartCommand;
import com.example.order_service.api.order.saga.orchestrator.event.SagaAbortEvent;
import com.example.order_service.api.order.saga.orchestrator.event.SagaResourceSecuredEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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

    @Test
    @DisplayName("주문 생성 이벤트를 수신하면 SAGA 를 수행한다")
    void handleOrderCreated() {
        //given
        OrderCreatedEvent.OrderedItem item1 = OrderCreatedEvent.OrderedItem.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        OrderCreatedEvent.OrderedItem item2 = OrderCreatedEvent.OrderedItem.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderNo(ORDER_NO)
                .userId(1L)
                .couponId(1L)
                .orderedItems(List.of(item1, item2))
                .usedPoint(1000L)
                .build();
        //when
        orderEventListener.handleOrderCreated(orderCreatedEvent);
        //then
        ArgumentCaptor<SagaStartCommand> captor = ArgumentCaptor.forClass(SagaStartCommand.class);
        verify(sagaManager, times(1)).startSaga(captor.capture());

        SagaStartCommand command = captor.getValue();

        assertThat(command)
                .extracting(SagaStartCommand::getOrderNo, SagaStartCommand::getUserId, SagaStartCommand::getCouponId, SagaStartCommand::getUsedPoint)
                .containsExactly(ORDER_NO, 1L, 1L, 1000L);
        assertThat(command.getDeductProductList())
                .hasSize(2)
                .extracting(SagaStartCommand.DeductProduct::getProductVariantId, SagaStartCommand.DeductProduct::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 3),
                        tuple(2L, 5)
                );
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

    @Test
    @DisplayName("Saga가 실패되면 주문 상태를 변경하기 위해 orderApplicationService를 호출한다")
    void handleSagaAborted() {
        //given
        SagaAbortEvent sagaAbortEvent = SagaAbortEvent.of(1L, ORDER_NO, 1L, OrderFailureCode.OUT_OF_STOCK);
        //when
        orderEventListener.handleSagaAborted(sagaAbortEvent);
        //then
        verify(orderFacade, times(1)).processOrderFailure(ORDER_NO, OrderFailureCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("결제 처리 후 Saga를 완료하기 위해 sagaManager를 호출한다")
    void handlePaymentResult(){
        //given
        PaymentResultEvent paymentResultEvent = PaymentResultEvent.of(ORDER_NO, 1L, OrderEventStatus.SUCCESS, null,
                List.of(1L, 2L));
        //when
        orderEventListener.handlePaymentResult(paymentResultEvent);
        //then
        ArgumentCaptor<SagaPaymentCommand> captor = ArgumentCaptor.forClass(SagaPaymentCommand.class);
        verify(sagaManager, times(1)).processPaymentResult(captor.capture());

        assertThat(captor.getValue())
                .extracting(SagaPaymentCommand::getOrderNo, SagaPaymentCommand::getStatus, SagaPaymentCommand::getCode,
                        SagaPaymentCommand::getFailureReason)
                .containsExactly(ORDER_NO, OrderEventStatus.SUCCESS, null, null);
    }
}
