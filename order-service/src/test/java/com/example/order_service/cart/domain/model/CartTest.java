package com.example.order_service.cart.domain.model;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

public class CartTest {


    @Nested
    @DisplayName("장바구니 상품 추가")
    class AddItem {

        @Test
        @DisplayName("항목을 추가한다")
        void addItem() {
            //given
            Cart cart = Cart.create(1L);
            Long productVariantId = 1L;
            int quantity = 3;
            //when
            cart.addItem(productVariantId, quantity);
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
            Cart cart = Cart.create(1L);
            Long productVariantId = 1L;
            cart.addItem(productVariantId, 3);
            //when
            cart.addItem(productVariantId, 2);
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
            Cart cart = Cart.create(1L);
            for (long i = 0; i < 20L; i++) {
                cart.addItem(i, 3);
            }
            //when
            //then
            assertThatThrownBy(() -> cart.addItem(999L, 3))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.EXCEED_AVAILABLE_CART_SIZE);
        }
    }

    @Nested
    @DisplayName("항목 수량 변경")
    class UpdateItemQuantity {

        @Test
        @DisplayName("항목 수량을 변경한다")
        void updateItemQuantity(){
            //given
            Cart cart = Cart.create(1L);
            Long cartItemId = 1L;
            CartItem cartItem = CartItem.create(1L, 3);
            ReflectionTestUtils.setField(cartItem, "id", cartItemId);
            cart.getCartItems().add(cartItem);
            //when
            cart.updateItemQuantity(cartItemId, 5);
            //then
            CartItem item = cart.findItemByCartItemId(cartItemId).orElseThrow();
            assertThat(item.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("항목을 찾을 수 없으면 예외가 발생한다")
        void updateItemQuantity_notFound_cartItem(){
            //given
            Cart cart = Cart.create(1L);
            //when
            //then
            assertThatThrownBy(() -> cart.updateItemQuantity(999L, 3))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 항목 삭제")
    class DeleteItem {

        @Test
        @DisplayName("항목을 삭제한다")
        void deleteItem(){
            //given
            Cart cart = Cart.create(1L);
            Long cartItemId = 1L;
            CartItem cartItem = CartItem.create(1L, 3);
            ReflectionTestUtils.setField(cartItem, "id", cartItemId);
            cart.getCartItems().add(cartItem);
            //when
            cart.deleteItem(cartItemId);
            //then
            assertThat(cart.getCartItems()).hasSize(0);
        }

        @Test
        @DisplayName("항목을 찾을 수 없으면 예외가 발생한다")
        void deleteItem_notFound_cartItem(){
            //given
            Cart cart = Cart.create(1L);
            //when
            //then
            assertThatThrownBy(() -> cart.deleteItem(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
    }
}
