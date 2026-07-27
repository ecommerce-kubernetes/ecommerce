package com.example.order_service.cart.domain;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class CartTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("장바구니를 생성한다.")
    void create(){
        //given
        Long userId = 1L;
        //when
        Cart cart = Cart.create(userId, idGenerator);
        //then
        assertThat(cart.getId()).isNotNull();
        assertThat(cart.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("장바구니를 생성할때 유저 아이디가 누락되면 예외가 발생한다.")
    void create_userId_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> Cart.create(null, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 생성시 유저 아이디는 필수입니다.");
    }

    @Test
    @DisplayName("항목을 추가한다")
    void addItem() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        Long productVariantId = 1L;
        int quantity = 3;
        //when
        cart.addItem(productVariantId, quantity, 100, idGenerator);
        //then
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems())
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(productVariantId, quantity)
                );
    }

    @Test
    @DisplayName("동일한 항목이 있다면 기존 항목의 수량을 증가시킨다")
    void addItemWhenExist() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        Long productVariantId = 1L;
        cart.addItem(productVariantId, 3, 100, idGenerator);
        //when
        cart.addItem(productVariantId, 2, 100, idGenerator);
        //then
        assertThat(cart.getCartItems()).hasSize(1);
        CartItem findItem = cart.findItemByProductVariantId(productVariantId).orElseThrow();
        assertThat(findItem)
                .extracting("productVariantId", "quantity")
                .contains(1L, 5);
    }

    @Test
    @DisplayName("장바구니 최대 항목이 초과한 경우 예외가 발생한다")
    void addItemExceedCartSize() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        for (long i = 0; i < 20L; i++) {
            cart.addItem(i, 3, 100, idGenerator);
        }
        //when
        //then
        assertThatThrownBy(() -> cart.addItem(999L, 3, 100, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_SIZE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("항목 수량을 변경한다")
    void updateItemQuantity(){
        //given
        Cart cart = Cart.create(1L, idGenerator);
        cart.addItem(1L, 3, 100, idGenerator);
        CartItem cartItem = cart.findItemByProductVariantId(1L).orElseThrow();
        //when
        cart.updateItemQuantity(cartItem.getId(), 5, 100);
        //then
        CartItem item = cart.findItemByCartItemId(cartItem.getId()).orElseThrow();
        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("항목을 찾을 수 없으면 예외가 발생한다")
    void updateItemQuantity_notFound_cartItem(){
        //given
        Cart cart = Cart.create(1L, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> cart.updateItemQuantity(999L, 3, 100))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("항목을 삭제한다")
    void deleteItem(){
        //given
        Cart cart = Cart.create(1L, idGenerator);
        cart.addItem(1L, 3, 100, idGenerator);
        CartItem cartItem = cart.findItemByProductVariantId(1L).orElseThrow();
        //when
        cart.deleteItem(cartItem.getId());
        //then
        assertThat(cart.getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("항목을 찾을 수 없으면 예외가 발생한다")
    void deleteItem_notFound_cartItem(){
        //given
        Cart cart = Cart.create(1L, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> cart.deleteItem(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
    }
}
