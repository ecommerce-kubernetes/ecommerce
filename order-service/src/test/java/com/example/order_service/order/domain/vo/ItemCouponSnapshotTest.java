package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemCouponSnapshotTest {

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
    @DisplayName("주문 항목의 수량이 쿠폰 최대 적용 가능 수량을 초과하는 경우 쿠폰 적용 수량은 쿠폰의 최대 적용 가능 수량이다.")
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