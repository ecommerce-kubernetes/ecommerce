package com.example.order_service.order.infrastructure.adaptor.in.listener;

import com.example.order_service.order.application.service.order.OrderCommandService;
import com.example.order_service.order.infrastructure.adaptor.in.listener.PaymentEventListener;
import com.example.order_service.payment.domain.event.PaymentCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @InjectMocks
    private PaymentEventListener listener;

    @Mock
    private OrderCommandService commandService;

    @Test
    @DisplayName("결제 완료 이벤트 수신시 주문 상태 변경을 호출한다.")
    void handlePaymentCompletedEvent() {
        //given
        PaymentCompletedEvent event = PaymentCompletedEvent.of(1L, 1L, 1L);
        //when
        listener.handlePaymentCompletedEvent(event);
        //then
        verify(commandService).changePaid(1L);
    }
}