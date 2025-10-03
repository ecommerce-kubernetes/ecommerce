package com.example.order_service.dto.response;

import com.example.order_service.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreateOrderResponse {
    private Long orderId;
    private String subscribeUrl;

    public CreateOrderResponse(Orders order, String subscribeUrl){
        this.orderId = order.getId();
        this.subscribeUrl = subscribeUrl;
    }
}
