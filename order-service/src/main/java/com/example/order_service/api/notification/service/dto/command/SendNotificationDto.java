package com.example.order_service.api.notification.service.dto.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendNotificationDto {
    private Long sendUserId;
    private String eventName;
    private Object sendObject;

    public static SendNotificationDto of(Long sendUserId, String eventName, Object sendObject) {
        return SendNotificationDto.builder()
                .sendUserId(sendUserId)
                .eventName(eventName)
                .sendObject(sendObject)
                .build();
    }
}
