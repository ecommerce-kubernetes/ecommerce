package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.model.Cart;
import com.example.order_service.api.cart.domain.model.CartItem;
import com.example.order_service.api.cart.domain.repository.CartRepository;
import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import com.example.order_service.api.support.ExcludeInfraTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Transactional
class CartServiceTest extends ExcludeInfraTest {
    @Autowired
    private CartService cartService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("장바구니 상품 추가")
    class AddItemToCart {

        @Test
        @DisplayName("처음 장바구니에 상품을 추가하면 장바구니를 생성하고 상품을 추가한다")
        void AddItemToCart_first_add(){
            //given
            //when
            CartItemDto result = cartService.addItemToCart(1L, 1L, 3);
            //then
            assertThat(result.getId()).isNotNull();
            assertThat(result)
                    .extracting(CartItemDto::getProductVariantId, CartItemDto::getQuantity)
                    .contains(1L, 3);

            Optional<Cart> cart = cartRepository.findByUserId(1L);
            assertThat(cart).isNotNull();
        }

        @Test
        @DisplayName("처음 이후 장바구니에 새로운 상품을 추가하면 기존 장바구니에 상품을 추가한다")
        void AddItemToCart_after_first_add(){
            //given
            Cart cart = Cart.create(1L);
            cartRepository.save(cart);
            //when
            CartItemDto result = cartService.addItemToCart(1L, 1L, 3);
            //then
            assertThat(result.getId()).isNotNull();
            assertThat(result)
                    .extracting(CartItemDto::getProductVariantId, CartItemDto::getQuantity)
                    .contains(1L, 3);
        }

        @Test
        @DisplayName("장바구니에 상품을 추가할때 추가하려는 상품이 이미 장바구니에 존재하는 상품이면 수량을 요청 수량만큼 증가시킨다")
        void AddItemToCart_exist_cart_item() {
            //given
            Cart cart = Cart.create(1L);
            CartItem existItem = cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            CartItemDto result = cartService.addItemToCart(1L, 1L, 2);
            //then
            assertThat(result.getId()).isNotNull();

            assertThat(result)
                    .extracting(CartItemDto::getId, CartItemDto::getProductVariantId, CartItemDto::getQuantity)
                    .contains(existItem.getId(), 1L, 5);
        }

        @Test
        @DisplayName("상품 수량이 1미만인 상품을 추가할 수 없다")
        void addItemToCart_quantity_less_than_1(){
            //given
            Cart cart = Cart.create(1L);
            cartRepository.save(cart);
            //when
            //then
            assertThatThrownBy(() -> cartService.addItemToCart(1L, 1L, 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 조회")
    class GetCartItem {

        @Test
        @DisplayName("장바구니 상품을 조회한다")
        void getCartItem() {
            //given
            Cart cart = Cart.create(1L);
            CartItem item = cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            CartItemDto cartItem = cartService.getCartItem(1L, item.getId());
            //then
            assertThat(cartItem.getId()).isEqualTo(item.getId());
            assertThat(cartItem.getProductVariantId()).isEqualTo(1L);
            assertThat(cartItem.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("장바구니 상품을 찾을 수 없을때 예외를 던진다")
        void getCartItem_not_found_cartItem() {
            //given
            //when
            //then
            assertThatThrownBy(() -> cartService.getCartItem(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("자신의 장바구니가 아닌 경우 장바구니 상품을 조회할 수 없다")
        void getCartItem_cart_userId_missMatch(){
            //given
            Cart cart = Cart.create(1L);
            CartItem item = cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            //then
            assertThatThrownBy(() -> cartService.getCartItem(999L, item.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_NO_PERMISSION);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 목록 조회")
    class GetCartItems {

        @Test
        @DisplayName("장바구니 상품 목록을 조회한다")
        void getCartItems(){
            //given
            Cart cart = Cart.create(1L);
            CartItem item1 = cart.addItem(1L, 3);
            CartItem item2 = cart.addItem(2L, 2);
            cartRepository.save(cart);
            //when
            List<CartItemDto> cartItems = cartService.getCartItems(1L);
            //then
            assertThat(cartItems).hasSize(2);
            assertThat(cartItems)
                    .extracting(CartItemDto::getId, CartItemDto::getProductVariantId, CartItemDto::getQuantity)
                    .containsExactlyInAnyOrder(
                            tuple(item1.getId(), 1L, 3),
                            tuple(item2.getId(), 2L, 2)
                    );
        }

        @Test
        @DisplayName("장바구니 상품 목록을 조회할때 해당 유저의 장바구니가 존재하지 않으면 빈 리스트를 반환한다")
        void getCartItemsWhenNotFoundCart(){
            //given
            //when
            List<CartItemDto> cartItems = cartService.getCartItems(1L);
            //then
            assertThat(cartItems).hasSize(0);
        }

        @Test
        @DisplayName("장바구니 상품 목록을 조회할때 해당 유저의 장바구니에 상품이 존재하지 않으면 빈 리스트를 반환한다")
        void getCatItemsWhenCartItemEmpty() {
            //given
            Cart cart = Cart.create(1L);
            cartRepository.save(cart);
            //when
            List<CartItemDto> cartItems = cartService.getCartItems(1L);
            //then
            assertThat(cartItems).hasSize(0);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 수량 변경")
    class UpdateQuantity {

        @Test
        @DisplayName("장바구니 상품의 수량을 변경한다")
        void updateQuantity(){
            //given
            Cart cart = Cart.create(1L);
            CartItem item = cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            CartItemDto cartItem = cartService.updateQuantity(1L, item.getId(), 5);
            //then
            assertThat(cartItem)
                    .extracting(CartItemDto::getId, CartItemDto::getProductVariantId, CartItemDto::getQuantity)
                    .containsExactly(item.getId(), 1L, 5);
        }

        @Test
        @DisplayName("자신의 장바구니가 아닌 경우 장바구니 상품 수량을 변경할 수 없다")
        void updateQuantity_cart_userId_miss_match(){
            //given
            Cart cart = Cart.create(1L);
            CartItem item = cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            //then
            assertThatThrownBy(() -> cartService.updateQuantity(999L, item.getId(), 5))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_NO_PERMISSION);
        }

        @Test
        @DisplayName("상품 수량은 1 미만으로 변경할 수 없다")
        void updateQuantity_quantity_less_than_1(){
            //given
            Cart cart = Cart.create(1L);
            CartItem item = cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            //then
            assertThatThrownBy(() -> cartService.updateQuantity(1L, item.getId(), 0))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 삭제")
    class DeleteCartItem {

        @Test
        @DisplayName("장바구니 상품을 삭제한다")
        void deleteCartItem(){
            //given
            Cart cart = Cart.create(1L);
            CartItem item1 = cart.addItem(1L, 3);
            CartItem item2 = cart.addItem(2L, 2);
            cartRepository.save(cart);
            //when
            cartService.deleteCartItem(1L, item1.getId());
            em.flush(); em.clear();
            //then
            Optional<Cart> findCart = cartRepository.findByUserId(1L);
            assertThat(findCart).isNotEmpty();
            assertThat(findCart.get().getCartItems()).hasSize(1)
                    .extracting(CartItem::getId, CartItem::getProductVariantId, CartItem::getQuantity)
                    .containsExactly(
                            tuple(item2.getId(), item2.getProductVariantId(), item2.getQuantity())
                    );
        }

        @Test
        @DisplayName("찾을 수 없는 상품은 삭제할 수 없다")
        void deleteCartItem_not_found_cartItem(){
            //given
            //when
            //then
            assertThatThrownBy(() -> cartService.deleteCartItem(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("자신의 장바구니가 아니면 장바구니 상품을 삭제할 수 없다")
        void deleteCartItem_cart_userId_missMatch(){
            //given
            Cart cart = Cart.create(1L);
            CartItem item = cart.addItem(1L, 3);
            cartRepository.save(cart);
            //when
            //then
            assertThatThrownBy(() -> cartService.deleteCartItem(999L, item.getId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_NO_PERMISSION);
        }

        @Test
        @DisplayName("상품 변형 Id로 장바구니에 있는 상품을 삭제한다")
        void deleteByProductVariantIds(){
            //given
            Cart cart = Cart.create(1L);
            cart.addItem(1L, 3);
            CartItem item2 = cart.addItem(2L, 5);
            cartRepository.save(cart);
            //when
            cartService.deleteByProductVariantIds(1L, List.of(1L));
            //then
            Optional<Cart> findCart = cartRepository.findWithItemsByUserId(1L);
            assertThat(findCart).isNotEmpty();
            assertThat(findCart.get().getCartItems()).hasSize(1);
            assertThat(findCart.get().getCartItems())
                    .extracting(CartItem::getId, CartItem::getProductVariantId, CartItem::getQuantity)
                    .containsExactly(
                            tuple(item2.getId(), item2.getProductVariantId(), item2.getQuantity())
                    );
        }
    }

    @Nested
    @DisplayName("장바구니 비우기")
    class ClearCart {

        @Test
        @DisplayName("장바구니를 모두 비운다")
        void clearCart(){
            //given
            Cart cart = Cart.create(1L);
            cart.addItem(1L, 2);
            cart.addItem(2L, 2);
            cartRepository.save(cart);
            //when
            cartService.clearCart(1L);
            //then
            Optional<Cart> findCart = cartRepository.findWithItemsByUserId(1L);
            assertThat(findCart).isNotEmpty();
            assertThat(findCart.get().getCartItems()).hasSize(0);
        }
    }
}