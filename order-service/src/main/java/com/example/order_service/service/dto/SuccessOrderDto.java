package com.example.order_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SuccessOrderDto {
    private Long orderId;
    private long originPrice;
    private long prodDiscount;
    private long couponDiscount;
    private long reserveDiscount;
    private long paymentAmount;
    private List<SuccessOrderItemDto> orderItems;
}
