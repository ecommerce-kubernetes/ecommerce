package com.example.order_service.api.notification.listener;

import com.example.order_service.api.notification.listener.dto.OrderNotificationDto;
import com.example.order_service.api.notification.service.NotificationService;
import com.example.order_service.api.order.application.event.OrderResultCode;
import com.example.order_service.api.order.application.event.OrderResultEvent;
import com.example.order_service.api.order.application.event.OrderResultStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @InjectMocks
    private NotificationListener notificationListener;
    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("주문 결정 이벤트를 수신하면 알림을 발송한다")
    void handleOrderResult() {
        //given
        OrderResultEvent event = OrderResultEvent.of(1L, 1L, OrderResultStatus.SUCCESS, OrderResultCode.PAYMENT_READY,
                "상품1 외 1건", 30000L,"결제 대기중입니다");
        //when
        notificationListener.handleOrderResult(event);
        //then
        ArgumentCaptor<OrderNotificationDto> listenerCaptor = ArgumentCaptor.forClass(OrderNotificationDto.class);
        verify(notificationService, times(1)).sendMessage(listenerCaptor.capture());

        assertThat(listenerCaptor.getValue())
                .extracting(OrderNotificationDto::getOrderId, OrderNotificationDto::getUserId,
                        OrderNotificationDto::getStatus, OrderNotificationDto::getCode,
                        OrderNotificationDto::getOrderName, OrderNotificationDto::getAmount, OrderNotificationDto::getMessage)
                .containsExactly(1L, 1L, "SUCCESS", "PAYMENT_READY", "상품1 외 1건", 30000L, "결제 대기중입니다");
    }
}