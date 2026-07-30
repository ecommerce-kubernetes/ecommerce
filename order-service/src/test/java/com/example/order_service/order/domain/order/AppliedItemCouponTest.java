package com.example.order_service.order.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class AppliedItemCouponTest {

    @Test
    @DisplayName("적용 상품 쿠폰을 생성한다.")
    void of(){
        //given
        Long itemCouponId = 1L;
        String name = "1000원 할인 쿠폰";
        //when
        AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(itemCouponId, name);
        //then
        assertThat(appliedItemCoupon)
                .extracting("itemCouponId", "name")
                .containsExactly(itemCouponId, name);
    }

    @Test
    @DisplayName("적용 상품 쿠폰 아이디가 누락되면 예외가 발생한다.")
    void of_itemCouponId_null(){
        //given
        String name = "1000원 할인 쿠폰";
        //when
        //then
        assertThatThrownBy(() -> AppliedItemCoupon.of(null, name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용 상품 쿠폰 아이디는 필수 입니다.");
    }

    @Test
    @DisplayName("적용 상품 쿠폰 이름이 누락되면 예외가 발생한다.")
    void of_name_null(){
        //given
        Long itemCouponId = 1L;
        //when
        //then
        assertThatThrownBy(() -> AppliedItemCoupon.of(itemCouponId, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용 상품 쿠폰 이름은 필수 입니다.");
    }
}