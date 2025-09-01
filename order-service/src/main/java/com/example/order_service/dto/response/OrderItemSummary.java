package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class OrderItemSummary {
    private List<OrderItemResponse> items;
    private int itemFinalPrice;
}
