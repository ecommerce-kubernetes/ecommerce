package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;

public class RateCouponDiscountPolicy implements CouponDiscountPolicy{
    @Override
    public Money calculateDiscount(Money amount) {
        return null;
    }
}
