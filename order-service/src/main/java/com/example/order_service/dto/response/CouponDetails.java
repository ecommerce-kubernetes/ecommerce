package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CouponDetails {
    private Long couponId;
    private int discountAmount;
}
