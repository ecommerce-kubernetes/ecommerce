package com.example.order_service.payment.adapter.in.listener;

import com.example.order_service.order.domain.order.event.OrderFailedEvent;
import com.example.order_service.payment.application.service.PaymentFacade;
import com.example.order_service.payment.application.service.dto.command.PaymentCancelCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @InjectMocks
    private OrderEventListener orderEventListener;

    @Mock
    private PaymentFacade paymentFacade;

    @Test
    @DisplayName("주문 실패 이벤트 수신시 결제 환불을 호출한다")
    void handleFailedOrderEvent(){
        //given
        OrderFailedEvent event = OrderFailedEvent.builder()
                .orderId(1L)
                .userId(1L)
                .reason("재고 부족")
                .build();
        //when
        orderEventListener.handleFailedOrderEvent(event);
        //then
        verify(paymentFacade).cancel(any(PaymentCancelCommand.class));
    }
}