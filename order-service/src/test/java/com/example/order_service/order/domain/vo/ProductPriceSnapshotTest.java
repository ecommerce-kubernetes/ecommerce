package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductPriceSnapshotTest {

    @Test
    @DisplayName("상품 가격 스냅샷을 생성한다")
    void of() {
        //given
        //when
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        //then
        assertThat(price)
                .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                .containsExactlyInAnyOrder(
                        Money.wons(10000L),
                        10,
                        Money.wons(1000L),
                        Money.wons(9000L)
                );
    }

    @Test
    @DisplayName("상품 원 가격이 누락되면 예외가 발생한다.")
    void of_originalPrice_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(null, 10, Money.wons(1000L), Money.wons(9000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 원 가격은 필수 입니다.");
    }

    @Test
    @DisplayName("상품 할인율이 누락되면 예외가 발생한다.")
    void of_discountRate_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(10000L), null, Money.wons(1000L), Money.wons(9000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 할인율은 필수 입니다.");
    }

    @Test
    @DisplayName("상품 할인 금액이 누락되면 예외가 발생한다.")
    void of_discountAmount_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(10000L), 10, null, Money.wons(9000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 할인 금액은 필수 입니다.");
    }

    @Test
    @DisplayName("상품 판매 가격이 누락되면 예외가 발생한다.")
    void of_discountedPrice_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 판매 가격은 필수 입니다.");
    }

    @Test
    @DisplayName("상품 할인율은 0~100 사이값이여야 한다.")
    void of_discountRate_between_0_and_100() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(10000L), 101, Money.wons(1000L), Money.wons(9000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_PRODUCT_DISCOUNT_RATE);
    }

    @Test
    @DisplayName("상품 원본 금액이 상품 할인 금액보다 작으면 예외가 발생한다")
    void of_original_price_less_than_discount_amount() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(100L), 10, Money.wons(1000L), Money.wons(9000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_PRODUCT_DISCOUNT_AMOUNT);
    }

    @Test
    @DisplayName("상품 판매가격이 상품 원본가격 - 상품 할인금액과 일치하지 않으면 예외가 발생한다")
    void of_discountedPrice_not_equal_originalPrice_subtract_discountAmount() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(100L), Money.wons(9000L)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_PRODUCT_DISCOUNTED_PRICE);
    }
}