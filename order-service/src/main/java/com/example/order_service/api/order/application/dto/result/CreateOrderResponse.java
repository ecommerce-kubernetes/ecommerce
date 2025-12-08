package com.example.order_service.api.order.application.dto.result;

import com.example.order_service.api.order.domain.model.Orders;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateOrderResponse {
    private Long orderId;
    private String status;
    private String message;
    private Integer totalQuantity;
    private Long finalPaymentAmount;
    private LocalDateTime createAt;

    //TODO 제거
    public CreateOrderResponse(Orders order, String subscribeUrl){
        this.orderId = order.getId();
    }

    @Builder
    private CreateOrderResponse(Long orderId, String status, String message, Integer totalQuantity, LocalDateTime createAt, Long finalPaymentAmount){
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.totalQuantity = totalQuantity;
        this.finalPaymentAmount = finalPaymentAmount;
        this.createAt = createAt;
    }
}
