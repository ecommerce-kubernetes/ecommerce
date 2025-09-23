package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DiscountSummary {
    private int prodDiscountAmount;
    private int couponDiscountAmount;
    private int reserveDiscountAmount;
}
