package com.example.order_service.order.application.event;

import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderFailedEvent {
    private String orderNo;
    private Long userId;
    private String code;
    private String orderName;

    public static OrderFailedEvent from(OrderDto orderDto) {
        return null;
    }
}
