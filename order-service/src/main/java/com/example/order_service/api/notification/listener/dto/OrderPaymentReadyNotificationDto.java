package com.example.order_service.api.notification.listener.dto;

import com.example.order_service.api.order.facade.event.OrderPaymentReadyEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderPaymentReadyNotificationDto {
    private String orderNo;
    private Long userId;
    private String code;
    private String orderName;
    private Long amount;


    public static OrderPaymentReadyNotificationDto from(OrderPaymentReadyEvent event) {
        return OrderPaymentReadyNotificationDto.builder()
                .orderNo(event.getOrderNo())
                .userId(event.getUserId())
                .code(event.getCode())
                .orderName(event.getOrderName())
                .amount(event.getFinalPaymentAmount())
                .build();
    }
}
