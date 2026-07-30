package com.example.order_service.order.domain.ordersheet;


import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartCouponSnapshotTest {

    @Test
    @DisplayName("장바구니 쿠폰을 생성한다.")
    void of() {
        //given
        Long cartCouponId = 1L;
        String name = "장바구니 1000원 할인";
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        Money minimumPaymentAmount = Money.wons(10000L);
        //when
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(cartCouponId, name, couponDiscountPolicy, minimumPaymentAmount);
        //then
        assertThat(cartCoupon)
                .extracting("cartCouponId", "name", "discountPolicy", "minimumPaymentAmount")
                .containsExactly(cartCouponId, name, couponDiscountPolicy, minimumPaymentAmount);
    }

    @Test
    @DisplayName("장바구니 쿠폰 아이디가 누락되면 예외가 발생한다.")
    void of_cartCouponId_null() {
        //given
        String name = "장바구니 1000원 할인";
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        Money minimumPaymentAmount = Money.wons(10000L);
        //when
        //then
        assertThatThrownBy(() -> CartCouponSnapshot.of(null, name, couponDiscountPolicy, minimumPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 쿠폰 아이디는 필수 입니다.");
    }

    @Test
    @DisplayName("장바구니 쿠폰 이름이 누락되면 예외가 발생한다.")
    void of_name_null() {
        //given
        Long cartCouponId = 1L;
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        Money minimumPaymentAmount = Money.wons(10000L);
        //when
        //then
        assertThatThrownBy(() -> CartCouponSnapshot.of(cartCouponId, null, couponDiscountPolicy, minimumPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 쿠폰 이름은 필수 입니다.");
    }

    @Test
    @DisplayName("쿠폰 할인 정책이 누락되면 예외가 발생한다.")
    void of_discountPolicy_null() {
        //given
        Long cartCouponId = 1L;
        String name = "장바구니 1000원 할인";
        Money minimumPaymentAmount = Money.wons(10000L);
        //when
        //then
        assertThatThrownBy(() -> CartCouponSnapshot.of(cartCouponId, name, null, minimumPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 쿠폰 할인 정책은 필수 입니다.");
    }

    @Test
    @DisplayName("최소 결제 금액이 누락되면 예외가 발생한다.")
    void of_minimumPaymentAmount_null() {
        //given
        Long cartCouponId = 1L;
        String name = "장바구니 1000원 할인";
        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        //when
        //then
        assertThatThrownBy(() -> CartCouponSnapshot.of(cartCouponId, name, couponDiscountPolicy, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 최소 결제 금액은 필수 입니다.");
    }

    @Test
    @DisplayName("기준 가격이 최소 결제 금액보다 작으면 쿠폰을 적용할 수 없다.")
    void isSatisfiedBy_baseAmount_less_than_minimumPaymentAmount(){
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
    @DisplayName("기준 가격이 최소 결제 금액보다 크면 쿠폰을 적용할 수 있다")
    void isSatisfiedBy_baseAmount_greater_than_minimumPaymentAmount(){
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