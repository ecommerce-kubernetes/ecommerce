package com.example.order_service.dto.response;

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
    private Long finalPaymentAmount;
    private LocalDateTime createAt;

    //TODO 제거
    public CreateOrderResponse(Orders order, String subscribeUrl){
        this.orderId = order.getId();
    }

    @Builder
    private CreateOrderResponse(Long orderId, String status, String message, LocalDateTime createAt, Long finalPaymentAmount){
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.finalPaymentAmount = finalPaymentAmount;
        this.createAt = createAt;
    }
}
