package com.example.order_service.cart.domain.model;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class CartTest {

    @Nested
    @DisplayName("장바구니 생성")
    class Create {

        @Test
        @DisplayName("장바구니를 생성한다")
        void create(){
            //given
            //when
            Cart cart = Cart.create(1L);
            //then
            assertThat(cart.getUserId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 추가")
    class AddItem {

        @Test
        @DisplayName("상품을 추가한다")
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
        @DisplayName("동일한 상품이 있다면 기존 상품에 수량을 증가시킨다")
        void addItemWhenExist(){
            //given
            Cart cart = Cart.create(1L);
            Long productVariantId = 1L;
            cart.addItem(productVariantId, 3);
            //when
            cart.addItem(productVariantId, 2);
            //then
            assertThat(cart.getCartItems()).hasSize(1);
            assertThat(cart.findItem(1L))
                    .extracting("productVariantId", "quantity")
                    .contains(1L, 5);
        }

        @Test
        @DisplayName("장바구니 최대 항목이 초과한 경우 예외가 발생한다")
        void addItemExceedCartSize() {
            //given
            Cart cart = Cart.create(1L);
            for(long i=0; i<20L; i++) {
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
    @DisplayName("장바구니 비우기")
    class ClearItems {

        @Test
        @DisplayName("장바구니의 모든 상품을 제거한다")
        void clearItems(){
            //given
            Cart cart = Cart.create(1L);
            cart.addItem(1L, 3);
            cart.addItem(2L, 6);
            //when
            cart.clearItems();
            //then
            assertThat(cart.getCartItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class RemoveItemsByVariantId {

        @Test
        @DisplayName("장바구니 상품들중 variantId와 동일한 상품을 제거한다")
        void removeItemsByVariantIds(){
            //given
            Cart cart = Cart.create(1L);
            cart.addItem(1L, 3);
            cart.addItem(2L, 6);
            //when
            cart.removeItemsByVariantIds(List.of(1L));
            //then
            assertThat(cart.getCartItems()).hasSize(1)
                    .extracting(CartItem::getProductVariantId, CartItem::getQuantity)
                    .containsExactly(
                            tuple(2L, 6)
                    );
        }
    }

    @Nested
    @DisplayName("장바구니 유저 검증")
    class IsOwner {

        @Test
        @DisplayName("장바구니의 userId 가 일치하면 true를 반환한다")
        void isOwner_true(){
            //given
            Cart cart = Cart.create(1L);
            //when
            boolean isOwner = cart.isOwner(1L);
            //then
            assertThat(isOwner).isTrue();
        }

        @Test
        @DisplayName("장바구니의 userId 가 일치하지 않으면 false를 반환한다")
        void isOwner_false(){
            //given
            Cart cart = Cart.create(1L);
            //when
            boolean isOwner = cart.isOwner(2L);
            //then
            assertThat(isOwner).isFalse();
        }
    }
}
