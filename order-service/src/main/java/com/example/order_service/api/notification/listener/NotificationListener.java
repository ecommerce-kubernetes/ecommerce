package com.example.order_service.api.notification.listener;

import com.example.order_service.api.notification.listener.dto.OrderFailedNotificationDto;
import com.example.order_service.api.notification.listener.dto.OrderPaymentReadyNotificationDto;
import com.example.order_service.api.notification.service.NotificationService;
import com.example.order_service.api.order.facade.event.OrderFailedEvent;
import com.example.order_service.api.order.facade.event.OrderPaymentReadyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @EventListener
    public void handlePaymentReadyEvent(OrderPaymentReadyEvent event){
        OrderPaymentReadyNotificationDto paymentReady = OrderPaymentReadyNotificationDto.from(event);
        notificationService.sendMessage(paymentReady);
    }

    @EventListener
    public void handleOrderFailedEvent(OrderFailedEvent event) {
        //TODO
        OrderFailedNotificationDto orderFailed = OrderFailedNotificationDto.from(event);
    }
}
