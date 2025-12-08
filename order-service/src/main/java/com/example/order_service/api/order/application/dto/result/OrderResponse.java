package com.example.order_service.api.order.application.dto.result;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private String status;
    private LocalDateTime createAt;
    private List<OrderItemResponse> orderItems;

    @Builder
    private OrderResponse(Long id, String status, LocalDateTime createAt, List<OrderItemResponse> orderItems){
        this.id = id;
        this.status = status;
        this.createAt = createAt;
        this.orderItems = orderItems;
    }

}
