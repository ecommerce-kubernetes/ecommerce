package com.example.order_service.order.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppliedCartCouponTest {

    @Test
    @DisplayName("적용 장바구니 쿠폰을 생성한다")
    void of(){
        //given
        Long cartCouponId = 1L;
        String name = "장바구니 1000원 할인 쿠폰";
        //when
        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(cartCouponId, name);
        //then
        assertThat(appliedCartCoupon)
                .extracting("cartCouponId", "name")
                .containsExactly(cartCouponId, name);
    }

    @Test
    @DisplayName("적용 장바구니 쿠폰을 생성할때 장바구니 쿠폰 아이디가 누락되면 예외가 발생한다.")
    void of_cartCouponId_null(){
        //given
        String name = "장바구니 1000원 할인 쿠폰";
        //when
        //then
        assertThatThrownBy(() -> AppliedCartCoupon.of(null, name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 쿠폰 아이디는 필수 입니다.");
    }

    @Test
    @DisplayName("적용 장바구니 쿠폰을 생성할때 장바구니 쿠폰 이름이 누락되면 예외가 발생한다.")
    void of_name_null(){
        //given
        Long cartCouponId = 1L;
        //when
        //then
        assertThatThrownBy(() -> AppliedCartCoupon.of(cartCouponId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 쿠폰 이름은 필수 입니다.");
    }
}