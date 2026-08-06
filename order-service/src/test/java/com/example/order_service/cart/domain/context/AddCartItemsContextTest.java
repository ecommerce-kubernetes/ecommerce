package com.example.order_service.cart.domain.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddCartItemsContextTest {

    @Test
    @DisplayName("장바구니 항목 목록이 없으면 예외가 발생한다.")
    void items_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> AddCartItemsContext.builder()
                .items(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니에 추가할 항목은 필수이다.");
    }

    @Test
    @DisplayName("장바구니 항목의 판매 단위 식별자가 누락되면 예외가 발생한다.")
    void items_productVariantId_null() {
        //given

        //when
        //then
        assertThatThrownBy(() -> AddCartItemsContext.Item.builder()
                .productVariantId(null)
                .quantity(1)
                .maxLimit(10)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 판매 단위 식별자는 필수이다.");
    }

    @Test
    @DisplayName("장바구니 항목의 수량이 누락되면 예외가 발생한다.")
    void items_quantity_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(null)
                .maxLimit(10)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 수량은 필수이다.");
    }

    @Test
    @DisplayName("장바구니 항목의 최대 추가 수량이 누락되면 예외가 발생한다.")
    void items_maxLimit_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(10)
                .maxLimit(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 최대 추가 수량은 필수이다.");
    }
}