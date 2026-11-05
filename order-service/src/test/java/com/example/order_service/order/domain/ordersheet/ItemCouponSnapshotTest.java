package com.example.order_service.order.domain.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemCouponSnapshotTest {

    @Test
    @DisplayName("상품 쿠폰을 생성한다.")
    void of() {
        //given
        Long itemCouponId = 1L;
        String name = "1000원 할인 쿠폰";
        CouponDiscountPolicy discountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        int applyQuantityLimit = 1;
        //when
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(itemCouponId, name, discountPolicy, applyQuantityLimit);
        //then
        assertThat(itemCoupon)
                .extracting("itemCouponId", "name", "discountPolicy", "applyQuantityLimit")
                .containsExactly(
                        itemCouponId, name, discountPolicy, applyQuantityLimit
                );
    }
    
    @Test
    @DisplayName("상품 쿠폰 아이디가 누락되면 예외가 발생한다.")
    void of_itemCouponId_null() {
        //given
        String name = "1000원 할인 쿠폰";
        CouponDiscountPolicy discountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        int applyQuantityLimit = 1;
        //when
        //then
        assertThatThrownBy(() -> ItemCouponSnapshot.of(null, name, discountPolicy, applyQuantityLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 쿠폰 아이디는 필수 입니다.");
    }

    @Test
    @DisplayName("상품 쿠폰 이름이 누락되면 예외가 발생한다.")
    void of_name() {
        //given
        Long itemCouponId = 1L;
        CouponDiscountPolicy discountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        int applyQuantityLimit = 1;
        //when
        //then
        assertThatThrownBy(() -> ItemCouponSnapshot.of(itemCouponId, null, discountPolicy, applyQuantityLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 쿠폰 이름은 필수 입니다.");
    }

    @Test
    @DisplayName("쿠폰 할인 정책이 누락되면 예외가 발생한다.")
    void of_discountPolicy_null() {
        //given
        Long itemCouponId = 1L;
        String name = "1000원 할인 쿠폰";
        int applyQuantityLimit = 1;
        //when
        //then
        assertThatThrownBy(() -> ItemCouponSnapshot.of(itemCouponId, name, null, applyQuantityLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 쿠폰 할인 정책은 필수 입니다.");
    }

    @Test
    @DisplayName("적용 가능 수량이 누락되면 예외가 발생한다.")
    void of_applyQuantityLimit() {
        //given
        Long itemCouponId = 1L;
        String name = "1000원 할인 쿠폰";
        CouponDiscountPolicy discountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        //when
        //then
        assertThatThrownBy(() -> ItemCouponSnapshot.of(itemCouponId, name, discountPolicy, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 쿠폰 적용 가능 수량은 필수 입니다.");
    }

    @Test
    @DisplayName("총 쿠폰 할인 금액을 계산한다.")
    void calculateTotalDiscount() {
        //given
        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, 1);
        //when
        Money result = itemCoupon.calculateTotalDiscount(Money.wons(10000L), 1);
        //then
        assertThat(result).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("수량이 쿠폰 최대 적용 가능 수량을 초과하는 경우 쿠폰 적용 수량은 쿠폰의 최대 적용 가능 수량이다.")
    void calculateTotalDiscount_exceed_applyQuantityLimit() {
        //given
        CouponDiscountPolicy policy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", policy, 2);
        //when
        Money result = itemCoupon.calculateTotalDiscount(Money.wons(10000L), 3);
        //then
        assertThat(result).isEqualTo(Money.wons(2000L));
    }

}