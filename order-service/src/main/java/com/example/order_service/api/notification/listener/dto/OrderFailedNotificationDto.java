package com.example.order_service.api.notification.listener.dto;

import com.example.order_service.api.order.facade.event.OrderFailedEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderFailedNotificationDto {
    private String orderNo;
    private Long userId;
    private String code;
    private String orderName;

    public static OrderFailedNotificationDto from(OrderFailedEvent event) {
        return OrderFailedNotificationDto.builder()
                .orderNo(event.getOrderNo())
                .userId(event.getUserId())
                .code(event.getCode())
                .orderName(event.getOrderName())
                .build();
    }
}
