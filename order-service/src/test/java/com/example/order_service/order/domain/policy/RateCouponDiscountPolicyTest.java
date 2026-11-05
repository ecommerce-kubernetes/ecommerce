package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateCouponDiscountPolicyTest {

    @Test
    @DisplayName("할인 금액을 계산한다")
    void calculateDiscount() {
        //given
        CouponDiscountPolicy policy = new RateCouponDiscountPolicy(10, Money.wons(10000L));
        Money target = Money.wons(10000L);
        //when
        Money result = policy.calculateDiscount(target);
        //then
        assertThat(result).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("계산된 할인 금액이 1원 단위라면 10원단위로 절삭한다")
    void calculateDiscount_truncate_Tens() {
        //given
        CouponDiscountPolicy policy = new RateCouponDiscountPolicy(10, Money.wons(10000L));
        Money target = Money.wons(10050L);
        //when
        Money result = policy.calculateDiscount(target);
        //then
        assertThat(result).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("할인 금액이 최대 할인 금액을 초과하는 경우 할인 금액은 최대 할인 금액이다")
    void calculateDiscount_exceed_maxDiscountAmount() {
        //given
        CouponDiscountPolicy policy = new RateCouponDiscountPolicy(10, Money.wons(10000L));
        Money target = Money.wons(1000000L);
        //when
        Money result = policy.calculateDiscount(target);
        //then
        assertThat(result).isEqualTo(Money.wons(10000L));
    }
}