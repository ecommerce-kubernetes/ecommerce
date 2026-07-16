package com.example.order_service.order.domain.vo;


import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartCouponSnapshotTest {

    @Test
    @DisplayName("주문 항목 총 가격이 최소 결제 금액보다 작으면 적용할 수 없다.")
    void isSatisfiedBy_totalPaymentAmount_less_than_minimumPaymentAmount(){
        //given
        Money minimumPaymentAmount = Money.wons(50000L);
        Money totalOrderAmount = Money.wons(30000L);
        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인", policy, minimumPaymentAmount);
        //when
        boolean result = cartCoupon.isSatisfiedBy(totalOrderAmount);
        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("주문 항목 총 가격이 최소 결제 금액보다 크면 적용할 수 있다")
    void isSatisfiedBy_totalPaymentAmount_greater_than_minimumPaymentAmount(){
        //given
        Money minimumPaymentAmount = Money.wons(10000L);
        Money totalOrderAmount = Money.wons(30000L);
        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인", policy, minimumPaymentAmount);
        //when
        boolean result = cartCoupon.isSatisfiedBy(totalOrderAmount);
        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("쿠폰 할인 금액을 계산한다.")
    void calculateDiscount(){
        //given
        Money totalOrderAmount = Money.wons(10000L);
        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "장바구니 1000원 할인", policy, Money.wons(10000L));
        //when
        Money discount = cartCoupon.calculateDiscount(totalOrderAmount);
        //then
        assertThat(discount).isEqualTo(Money.wons(1000L));
    }
}