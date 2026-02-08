package com.example.order_service.api.order.facade.dto.result;

import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderResponse {
    private String orderNo;
    private String status;
    private String orderName;
    private Long finalPaymentAmount;
    private String createdAt;

    @Builder
    private CreateOrderResponse(String orderNo, String status, String orderName, String createdAt, Long finalPaymentAmount){
        this.orderNo = orderNo;
        this.status = status;
        this.orderName = orderName;
        this.finalPaymentAmount = finalPaymentAmount;
        this.createdAt = createdAt;
    }

    public static CreateOrderResponse from(OrderDto orderDto) {
        return CreateOrderResponse.builder()
                .orderNo(orderDto.getOrderNo())
                .status(orderDto.getStatus().name())
                .orderName(orderDto.getOrderName())
                .finalPaymentAmount(orderDto.getOrderPriceInfo().getFinalPaymentAmount())
                .createdAt(orderDto.getOrderedAt().toString())
                .build();
    }
}
