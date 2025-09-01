package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderSummary {
    private int productTotal;
    private int discount;
    private int finalPayment;
}
