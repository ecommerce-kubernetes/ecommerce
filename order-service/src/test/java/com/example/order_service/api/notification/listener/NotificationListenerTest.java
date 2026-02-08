package com.example.order_service.api.notification.listener;

import com.example.order_service.api.notification.service.NotificationService;
import com.example.order_service.api.notification.service.dto.command.SendNotificationDto;
import com.example.order_service.api.order.facade.event.OrderFailedEvent;
import com.example.order_service.api.order.facade.event.OrderPaymentReadyEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @InjectMocks
    private NotificationListener notificationListener;
    @Mock
    private NotificationService notificationService;
    @Captor
    private ArgumentCaptor<SendNotificationDto> captor;

    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Test
    @DisplayName("결제 대기 이벤트 수신시 메시지 발행")
    void handlePaymentReadyEvent(){
        //given
        OrderPaymentReadyEvent event = OrderPaymentReadyEvent.builder()
                .orderNo(ORDER_NO)
                .userId(1L)
                .code("PAYMENT_READY")
                .orderName("상품")
                .finalPaymentAmount(9000L)
                .build();
        //when
        notificationListener.handlePaymentReadyEvent(event);
        //then
        verify(notificationService).sendMessage(captor.capture());
        assertThat(captor.getValue())
                .extracting(SendNotificationDto::getSendUserId, SendNotificationDto::getEventName)
                .containsExactly(1L, "ORDER_RESULT");
    }

    @Test
    @DisplayName("주문 실패 이벤트 수신시 메시지 발행")
    void handleOrderFailedEvent(){
        //given
        OrderFailedEvent event = OrderFailedEvent.builder()
                .orderNo(ORDER_NO)
                .userId(1L)
                .orderName("상품")
                .code("ORDER_FAILED")
                .build();
        //when
        notificationListener.handleOrderFailedEvent(event);
        //then
        verify(notificationService).sendMessage(captor.capture());
        assertThat(captor.getValue())
                .extracting(SendNotificationDto::getSendUserId, SendNotificationDto::getEventName)
                .containsExactly(1L, "ORDER_RESULT");
    }

}