package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixedCouponDiscountPolicyTest {

    @Test
    @DisplayName("할인 금액을 계산한다.")
    void calculateDiscount() {
        //given
        FixedCouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        Money target = Money.wons(10000L);
        //when
        Money result = policy.calculateDiscount(target);
        //then
        assertThat(result).isEqualTo(Money.wons(1000L));
    }
}