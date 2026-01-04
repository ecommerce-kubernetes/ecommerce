package com.example.order_service.api.notification.service.dto;

import com.example.order_service.api.notification.listener.dto.OrderNotificationDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderNotificationResponse {
    private String orderNo;
    private Long userId;
    private String status;
    private String code;
    private String orderName;
    private Long amount;
    private String message;

    @Builder
    private OrderNotificationResponse(String orderNo, Long userId, String status, String code, String orderName, Long amount, String message) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.status = status;
        this.code = code;
        this.orderName = orderName;
        this.amount = amount;
        this.message = message;
    }

    public static OrderNotificationResponse from(OrderNotificationDto dto) {
        return OrderNotificationResponse.builder()
                .orderNo(dto.getOrderNo())
                .userId(dto.getUserId())
                .status(dto.getStatus())
                .code(dto.getCode())
                .orderName(dto.getOrderName())
                .amount(dto.getAmount())
                .build();
    }
}
