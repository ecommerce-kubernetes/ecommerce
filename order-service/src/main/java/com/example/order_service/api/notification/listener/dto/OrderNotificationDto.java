package com.example.order_service.api.notification.listener.dto;

import com.example.order_service.api.order.facade.event.OrderResultEvent;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderNotificationDto {
    private String orderNo;
    private Long userId;
    private String status;
    private String code;
    private String orderName;
    private Long amount;
    private String message;

    @Builder
    private OrderNotificationDto(String orderNo, Long userId, String status, String code, String orderName, Long amount, String message) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.status = status;
        this.code = code;
        this.orderName = orderName;
        this.amount = amount;
        this.message = message;
    }

    public static OrderNotificationDto from(OrderResultEvent event) {
        return OrderNotificationDto.builder()
                .orderNo(event.getOrderNo())
                .userId(event.getUserId())
                .status(event.getStatus().name())
                .code(event.getCode())
                .orderName(event.getOrderName())
                .amount(event.getFinalPaymentAmount())
                .message(event.getMessage())
                .build();
    }
}
