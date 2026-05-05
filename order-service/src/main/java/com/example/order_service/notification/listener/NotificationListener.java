package com.example.order_service.notification.listener;

import com.example.order_service.notification.service.NotificationService;
import com.example.order_service.notification.service.dto.command.SendNotificationDto;
import com.example.order_service.order.application.event.OrderFailedEvent;
import com.example.order_service.order.application.event.OrderPaymentReadyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @EventListener
    public void handlePaymentReadyEvent(OrderPaymentReadyEvent event){
        SendNotificationDto dto = SendNotificationDto.of(event.getUserId(), "ORDER_RESULT", event);
        notificationService.sendMessage(dto);
    }

    @EventListener
    public void handleOrderFailedEvent(OrderFailedEvent event) {
        SendNotificationDto dto = SendNotificationDto.of(event.getUserId(), "ORDER_RESULT", event);
        notificationService.sendMessage(dto);
    }
}
