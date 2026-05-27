package com.example.order_service.order.application.event;

import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderPaymentReadyEvent {
    private String orderNo;
    private Long userId;
    private String code;
    private String orderName;
    private Long finalPaymentAmount;

    public static OrderPaymentReadyEvent from(OrderDto orderDto) {
        return null;
    }
}
