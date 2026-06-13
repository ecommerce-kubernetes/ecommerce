package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPrice")
    void of_null(String description, Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(originalPrice, discountRate, discountAmount, discountedPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 가격 정보 누락");
    }

    @ParameterizedTest(name = "할인율은 0~100 사이값이다")
    @CsvSource(
            value = {"-1, 101"},
            nullValues = "null"
    )
    void of_discountRate_between_0_and_100(Integer discountRate) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(10000L), discountRate, Money.wons(1000L), Money.wons(9000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("할인율은 0 ~ 100 의 값이여야 합니다");
    }

    @Test
    @DisplayName("상품 원본 금액이 상품 할인 금액보다 작으면 예외가 발생한다")
    void of_original_price_less_than_discount_amount() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(100L), 10, Money.wons(1000L), Money.wons(9000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 할인 금액이 상품 금액을 초과할 수 없습니다");
    }

    @Test
    @DisplayName("상품 판매가격이 상품 원본가격 - 상품 할인금액과 일치하지 않으면 예외가 발생한다")
    void of_discountedPrice_not_equal_originalPrice_subtract_discountAmount() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(100L), Money.wons(9000L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 판매 가격이 올바르지 않습니다");
    }

    private static Stream<Arguments> invalidPrice() {
        return Stream.of(
                Arguments.of(
                        "상품 원본 가격 null" ,null, 10, Money.wons(1000L), Money.wons(9000L)
                ),
                Arguments.of(
                        "상품 할인율 null", Money.wons(10000L), null, Money.wons(1000L), Money.wons(9000L)
                ),
                Arguments.of(
                        "상품 할인 가격 null",Money.wons(10000L), 10, null, Money.wons(9000L)
                ),
                Arguments.of(
                        "상품 판매 가격 null", Money.wons(10000L), 10,  Money.wons(1000L),  null
                )
        );
    }
}