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

    public static CreateOrderResponse of(OrderDto result) {
        return CreateOrderResponse.builder()
                .orderNo(result.getOrderNo())
                .status(result.getStatus().name())
                .orderName(result.getOrderName())
                .finalPaymentAmount(result.getOrderPriceDetail().getFinalPaymentAmount())
                .createdAt(result.getOrderedAt().toString())
                .build();
    }
}
