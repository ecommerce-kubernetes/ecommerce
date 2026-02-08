package com.example.order_service.api.order.facade.event;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
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
        return OrderFailedEvent.builder()
                .orderNo(orderDto.getOrderNo())
                .userId(orderDto.getOrderer().getUserId())
                .code(orderDto.getOrderFailureCode().name())
                .orderName(orderDto.getOrderName())
                .build();
    }
}
