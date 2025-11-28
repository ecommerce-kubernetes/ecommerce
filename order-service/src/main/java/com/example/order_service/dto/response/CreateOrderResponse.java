package com.example.order_service.dto.response;

import com.example.order_service.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderResponse {
    private Long orderId;
    private String subscribeUrl;

    public CreateOrderResponse(Orders order, String subscribeUrl){
        this.orderId = order.getId();
        this.subscribeUrl = subscribeUrl;
    }

    @Builder
    private CreateOrderResponse(Long orderId, String subscribeUrl){
        this.orderId = orderId;
        this.subscribeUrl = subscribeUrl;
    }
}
