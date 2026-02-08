package com.example.order_service.api.cart.domain.model;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CartItemTest {

    @Nested
    @DisplayName("장바구니 상품 생성")
    class Create {

        @Test
        @DisplayName("장바구니 상품을 생성한다")
        void create(){
            //given
            //when
            CartItem item = CartItem.create(1L, 3);
            //then
            assertThat(item)
                    .extracting(CartItem::getProductVariantId, CartItem::getQuantity)
                    .containsExactly(1L, 3);
        }

        @Test
        @DisplayName("수량이 1미만인 상품은 생성할 수 없다")
        void create_quantity_less_than_1(){
            //given
            //when
            //then
            assertThatThrownBy(() -> CartItem.create(1L, 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED);
        }
    }

    @Nested
    @DisplayName("상품 수량 변경")
    class UpdateQuantity {

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
        @DisplayName("상품 수량을 1 미만으로 변경할 수 없다")
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
}
