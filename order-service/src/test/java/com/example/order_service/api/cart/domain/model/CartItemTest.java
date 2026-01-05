package com.example.order_service.api.cart.domain.model;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CartItemTest {

    @Test
    @DisplayName("장바구니 상품의 수량을 변경한다")
    void updateQuantity(){
        //given
        CartItem cartItem = CartItem.create(1L, 3);
        //when
        cartItem.updateQuantity(5);
        //then
        assertThat(cartItem.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품 수량을 1 이하로 변경하려 시도하면 예외를 던진다")
    void updateQuantityWhenQuantityLessThan1(){
        //given
        CartItem cartItem = CartItem.create(1L, 3);
        //when
        //then
        assertThatThrownBy(() -> cartItem.updateQuantity(0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED);
    }
}
