package com.example.order_service.api.order.facade.event;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
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
        return OrderPaymentReadyEvent
                .builder()
                .orderNo(orderDto.getOrderNo())
                .userId(orderDto.getOrderer().getUserId())
                .code(orderDto.getStatus().name())
                .orderName(orderDto.getOrderName())
                .finalPaymentAmount(orderDto.getOrderPriceDetail().getFinalPaymentAmount())
                .build();
    }
}
