package com.example.order_service.api.cart.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CartTest {

    @Test
    @DisplayName("동일한 상품이 없다면 새로운 상품을 생성해 추가한다")
    void addItemWhenNew() {
        //given
        Cart cart = Cart.create(1L);
        //when
        CartItem cartItem = cart.addItem(1L, 2);
        //then
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cartItem)
                .extracting("productVariantId", "quantity")
                .contains(1L, 2);
    }

    @Test
    @DisplayName("동일한 상품이 있다면 기존 상품에 수량을 증가시킨다")
    void addItemWhenExist(){
        //given
        Cart cart = Cart.create(1L);
        cart.getCartItems().add(CartItem.create(1L, 2));
        //when
        CartItem cartItem = cart.addItem(1L, 3);
        //then
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cartItem)
                .extracting("productVariantId", "quantity")
                .contains(1L, 5);
    }
}
