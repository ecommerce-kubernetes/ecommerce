package com.example.order_service.api.notification.listener;

import com.example.order_service.api.notification.listener.dto.OrderNotificationDto;
import com.example.order_service.api.notification.service.NotificationService;
import com.example.order_service.api.order.application.event.OrderResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleOrderResult(OrderResultEvent event){
        OrderNotificationDto orderResult = OrderNotificationDto.from(event);
        notificationService.sendMessage(orderResult);
    }
}
