package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartCouponSnapshotTest {

    @Test
    @DisplayName("쿠폰 스냅샷을 생성한다")
    void of() {
        //given
        //when
        CartCouponSnapshot coupon = CartCouponSnapshot.of(1L, "1000원 할인 쿠폰", Money.wons(1000L));
        //then
        assertThat(coupon)
                .extracting("couponId", "couponName", "discountAmount")
                .containsExactlyInAnyOrder(
                        1L, "1000원 할인 쿠폰", Money.wons(1000L)
                );
    }

    @Test
    @DisplayName("적용 쿠폰 아이디가 null이면 예외가 발생한다")
    void of_couponId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CartCouponSnapshot.of(null, "1000원 할인 쿠폰", Money.wons(1000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용 쿠폰 Id 는 필수 입니다");
    }

    @ParameterizedTest(name = "쿠폰 이름이 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_couponName(String couponName) {
        //given
        //when
        //then
        assertThatThrownBy(() -> CartCouponSnapshot.of(1L, couponName, Money.wons(1000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용 쿠폰 이름은 필수입니다");
    }

    @Test
    @DisplayName("쿠폰 할인금액이 유효하지 않으면 예외가 발생한다")
    void of_invalid_couponDiscountAmount() {
        //given
        //when
        //then
        assertThatThrownBy(() -> CartCouponSnapshot.of(1L, "1000원 할인 쿠폰", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용 쿠폰 할인금은 0원 이상이여야 합니다");
    }

    @Test
    @DisplayName("쿠폰 미적용")
    void empty() {
        //given
        //when
        CartCouponSnapshot empty = CartCouponSnapshot.empty();
        //then
        assertThat(empty)
                .extracting("couponId", "couponName", "discountAmount")
                .containsExactlyInAnyOrder(
                        null, "쿠폰 미적용", Money.ZERO
                );
    }
}