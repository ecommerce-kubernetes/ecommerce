package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class RateCouponDiscountPolicy implements CouponDiscountPolicy{
    private int discountRate;
    private Money maxDiscountAmount;

    public RateCouponDiscountPolicy(int discountRate, Money maxDiscountAmount) {
        this.discountRate = discountRate;
        this.maxDiscountAmount = maxDiscountAmount;
    }

    @Override
    public Money calculateDiscount(Money amount) {
        Money discountAmount = amount.multiple(discountRate / 100.0);
        if (discountAmount.isGreaterThan(maxDiscountAmount)) {
            return maxDiscountAmount.truncateToTens();
        }
        return discountAmount.truncateToTens();
    }
}
