package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;

public interface CouponDiscountPolicy {
    Money calculateDiscount(Money amount);
}
