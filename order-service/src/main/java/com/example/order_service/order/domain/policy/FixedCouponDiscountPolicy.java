package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
public class FixedCouponDiscountPolicy implements CouponDiscountPolicy {
    private Money discountAmount;

    public FixedCouponDiscountPolicy(Money discountAmount) {
        this.discountAmount = discountAmount;
    }

    @Override
    public Money calculateDiscount(Money amount) {
        return discountAmount;
    }
}
