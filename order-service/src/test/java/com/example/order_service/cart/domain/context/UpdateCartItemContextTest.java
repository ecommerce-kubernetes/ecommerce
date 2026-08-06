package com.example.order_service.cart.domain.context;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateCartItemContextTest {

    @Test
    @DisplayName("장바구니 항목 아이디가 누락되면 예외가 발생한다.")
    void cartItemId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> UpdateCartItemContext.builder()
                .cartItemId(null)
                .quantity(3)
                .maxLimit(100)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 수량 변경시 장바구니 항목 식별자는 필수이다.");
    }
    
    @Test
    @DisplayName("변경 수량이 누락되면 예외가 발생한다.")
    void quantity_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> UpdateCartItemContext.builder()
                .cartItemId(1L)
                .quantity(null)
                .maxLimit(100)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 수량 변경시 변경 수량은 필수이다.");
    }

    @Test
    @DisplayName("장바구니 항목 최대 수량이 누락되면 예외가 발생한다.")
    void maxLimit_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> UpdateCartItemContext.builder()
                .cartItemId(1L)
                .quantity(10)
                .maxLimit(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 항목 최대 수량은 필수이다.");
    }
}