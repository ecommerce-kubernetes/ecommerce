package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.domain.model.Cart;
import com.example.order_service.cart.domain.model.CartItem;
import com.example.order_service.cart.domain.repository.CartRepository;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@IsolatedTest
@Transactional
class CartQueryServiceTest {

    @Autowired
    private CartQueryService cartQueryService;
    @Autowired
    private CartRepository cartRepository;

    @Nested
    @DisplayName("장바구니 항목 조회")
    class FindCartItems {

        @Test
        @DisplayName("장바구니의 전체 항목 정보를 반환한다.")
        void findCartItems() {
            //given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            cart.addItem(1L, 2);
            cart.addItem(2L, 3);
            cartRepository.save(cart);
            //when
            List<CartItemData> cartItems = cartQueryService.findCartItems(userId);
            //then
            assertThat(cartItems).hasSize(2);

            assertThat(cartItems)
                    .allSatisfy(item -> assertThat(item.cartItemId()).isNotNull());

            assertThat(cartItems)
                    .extracting(CartItemData::productVariantId, CartItemData::quantity)
                    .containsExactly(
                            tuple(1L, 2),
                            tuple(2L, 3)
                    );
        }

        @Test
        @DisplayName("장바구니가 존재하지 않으면 빈 리스트를 반환한다")
        void findCartItems_notFound_cart() {
            //given
            Long userId = 1L;
            //when
            List<CartItemData> cartItems = cartQueryService.findCartItems(userId);
            //then
            assertThat(cartItems).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("장바구니 항목 조회")
    class FindCartItemsByVariantIds {
        
        @Test
        @DisplayName("상품 변형 Id로 장바구니 항목을 조회한다")
        void findCartItemsByVariantIds() {
            //given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            cart.addItem(1L, 3);
            cart.addItem(2L, 2);
            cartRepository.save(cart);
            //when
            List<CartItemData> cartItems = cartQueryService.findCartItemsByVariantIds(userId, List.of(1L));
            //then
            assertThat(cartItems).hasSize(1);
            assertThat(cartItems)
                    .extracting(CartItemData::productVariantId, CartItemData::quantity)
                    .containsExactly(
                            tuple(1L, 3)
                    );
        }

        @Test
        @DisplayName("장바구니 항목을 찾을 수 없으면 반환 리스트에 제외하고 반환한다")
        void findCartItemsByVariantIds_notFound_cartItem() {
            //given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            List<CartItemData> cartItems = cartQueryService.findCartItemsByVariantIds(userId, List.of(1L, 2L));
            //then
            assertThat(cartItems).hasSize(1);
            assertThat(cartItems)
                    .extracting(CartItemData::productVariantId, CartItemData::quantity)
                    .containsExactly(
                            tuple(1L, 3)
                    );
        }

        @Test
        @DisplayName("장바구니가 존재하지 않으면 빈 리스트를 반환한다")
        void findCartItemsByVariantIds_notFound_cart() {
            //given
            Long userId = 1L;
            //when
            List<CartItemData> cartItems = cartQueryService.findCartItemsByVariantIds(userId, List.of(999L));
            //then
            assertThat(cartItems).isEmpty();
        }
    }

    @Nested
    @DisplayName("장바구니 항목 조회")
    class GetCartItem {

        @Test
        @DisplayName("장바구니 항목을 장바구니 항목 Id로 조회한다")
        void getCartItem() {
            //given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            cart.addItem(1L, 3);
            cartRepository.save(cart);
            CartItem cartItem = cart.findItemByProductVariantId(1L).orElseThrow();
            //when
            CartItemData getCartItem = cartQueryService.getCartItem(userId, cartItem.getId());
            //then
            assertThat(getCartItem)
                    .extracting(CartItemData::productVariantId, CartItemData::quantity)
                    .containsExactly(
                            cartItem.getId(), cartItem.getQuantity()
                    );
        }

        @Test
        @DisplayName("장바구니가 존재하지 않으면 예외가 발생한다")
        void getCartItem_notFound_cart() {
            //given
            //when
            //then
            assertThatThrownBy(() -> cartQueryService.getCartItem(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_NOT_FOUND);
        }

        @Test
        @DisplayName("장바구니에 항목이 존재하지 않으면 예외가 발생한다")
        void getCartItem_notFound_cartItem() {
            //given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            cartRepository.save(cart);
            //when
            //then
            assertThatThrownBy(() -> cartQueryService.getCartItem(userId, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
        }
    }
}