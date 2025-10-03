package com.example.order_service.service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CouponResponse {
    private String discountType;
    private int discountValue;
    private int minPurchaseAmount;
    private int maxDiscountAmount;
}
