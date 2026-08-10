package com.example.order_service.payment.infrastructure.adaptor.event;

import com.example.order_service.payment.domain.event.PaymentApprovedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventAdaptorTest {

    @InjectMocks
    private PaymentEventAdaptor paymentEventAdaptor;

    @Mock
    private ApplicationEventPublisher publisher;
    
    @Test
    @DisplayName("결제 승인 이벤트를 발행한다.")
    void publishApproved() {
        //given
        PaymentApprovedEvent event = PaymentApprovedEvent.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(1L)
                .build();
        //when
        paymentEventAdaptor.publishApproved(event);
        //then
        verify(publisher).publishEvent(event);
    }

}