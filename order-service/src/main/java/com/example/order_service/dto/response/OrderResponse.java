package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String status;
    private LocalDateTime createAt;
    private String deliveryAddress;
    private OrderSummary orderSummary;
    private PaymentDetails paymentDetails;
    private OrderItemSummary orderItemSummary;
}
