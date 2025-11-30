package com.example.order_service.dto.response;

import com.example.order_service.entity.Orders;
import lombok.AllArgsConstructor;
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
    private LocalDateTime createAt;
    private String subscribeUrl;

    public CreateOrderResponse(Orders order, String subscribeUrl){
        this.orderId = order.getId();
        this.subscribeUrl = subscribeUrl;
    }

    @Builder
    private CreateOrderResponse(Long orderId, String status, String message, LocalDateTime createAt, String subscribeUrl){
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.createAt = createAt;
        this.subscribeUrl = subscribeUrl;
    }
}
