package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.domain.model.Cart;
import com.example.order_service.cart.domain.model.CartItem;
import com.example.order_service.cart.domain.repository.CartRepository;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.support.annotation.IsolatedTest;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@IsolatedTest
@Transactional
class CartCommandServiceTest {

    @Autowired
    private CartCommandService cartCommandService;
    @Autowired
    private CartRepository cartRepository;

    @Nested
    @DisplayName("장바구니 상품을 추가한다")
    class AddCartItems {

        @Test
        @DisplayName("장바구니가 존재하지 않는 경우 장바구니를 생성한 뒤 상품을 추가한다")
        void addCartItems_not_exist_cart(){
            //given
            AddCartItemsCommand.Item item = AddCartItemsCommand.Item.builder()
                    .productVariantId(1L)
                    .quantity(3)
                    .build();
            AddCartItemsCommand command = AddCartItemsCommand.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .build();
            //when
            cartCommandService.addCartItems(command);
            //then
            Cart cart = cartRepository.findByUserId(command.userId()).orElseThrow();
            assertThat(cart.getCartItems()).hasSize(1);
            assertThat(cart.getCartItems()).allSatisfy(cartItem ->
                            assertThat(cartItem.getId()).isNotNull());
            assertThat(cart.getCartItems())
                    .extracting(CartItem::getProductVariantId, CartItem::getQuantity)
                    .containsExactlyInAnyOrder(
                            tuple(item.productVariantId(), item.quantity())
                    );
        }

        @Test
        @DisplayName("장바구니가 존재하는 경우 기존 장바구니에 상품을 추가한다")
        void addCartItems_exist_cart(){
            //given
            Long userId = 1L;
            AddCartItemsCommand.Item item = AddCartItemsCommand.Item.builder()
                    .productVariantId(1L)
                    .quantity(3)
                    .build();
            AddCartItemsCommand command = AddCartItemsCommand.builder()
                    .userId(userId)
                    .items(List.of(item))
                    .build();
            cartRepository.save(Cart.create(userId));
            //when
            cartCommandService.addCartItems(command);
            //then
            Cart cart = cartRepository.findByUserId(userId).orElseThrow();
            assertThat(cart.getCartItems()).hasSize(1);
            assertThat(cart.getCartItems())
                    .extracting(CartItem::getProductVariantId, CartItem::getQuantity)
                    .containsExactlyInAnyOrder(
                            tuple(1L, 3)
                    );
        }

        @Test
        @DisplayName("장바구니에 동일한 상품이 존재하는 경우 수량을 증가시킨다")
        void addCartItems_duplicate_item(){
            //given
            Long userId = 1L;
            Long productVariantId = 1L;
            int existQuantity = 3;
            int addQuantity = 2;

            AddCartItemsCommand.Item item = AddCartItemsCommand.Item.builder()
                    .productVariantId(productVariantId)
                    .quantity(addQuantity)
                    .build();

            AddCartItemsCommand command = AddCartItemsCommand.builder()
                    .userId(userId)
                    .items(List.of(item))
                    .build();

            Cart cart = Cart.create(userId);
            cart.addItem(productVariantId, existQuantity);
            cartRepository.save(cart);
            //when
            cartCommandService.addCartItems(command);
            //then
            Cart findCart = cartRepository.findByUserId(userId).orElseThrow();
            assertThat(findCart.getCartItems()).hasSize(1);
            assertThat(findCart.getCartItems())
                    .extracting(CartItem::getProductVariantId, CartItem::getQuantity)
                    .containsExactlyInAnyOrder(
                            tuple(productVariantId, existQuantity + addQuantity)
                    );
        }
    }

    @Nested
    @DisplayName("장바구니 항목 수량 변경")
    class UpdateCartItemQuantity {

        @Test
        @DisplayName("장바구니 항목 수량을 변경한다")
        void updateCartItemQuantity(){
            //given
            Long userId = 1L;
            Cart cart = Cart.create(userId);
            cart.addItem(1L, 3);
            cartRepository.save(cart);

            CartItem item = cart.findItemByProductVariantId(1L).orElseThrow();
            UpdateCartItemQuantityCommand command = UpdateCartItemQuantityCommand.builder()
                    .userId(userId)
                    .cartItemId(item.getId())
                    .quantity(2)
                    .build();
            //when
            cartCommandService.updateCartItemQuantity(command);
            //then
            Cart findCart = cartRepository.findByUserId(userId).orElseThrow();
            CartItem findItem = findCart.findItemByCartItemId(item.getId()).orElseThrow();
            assertThat(findItem.getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("장바구니를 찾을 수 없으면 예외가 발생한다")
        void updateCartItemQuantity_notFound_cart(){
            //given
            UpdateCartItemQuantityCommand command = UpdateCartItemQuantityCommand.builder()
                    .userId(1L)
                    .cartItemId(1L)
                    .quantity(3)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> cartCommandService.updateCartItemQuantity(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("장바구니 항목 삭제")
    class DeleteCartItems {

        @Test
        @DisplayName("장바구니에 담긴 항목을 제거한다")
        void deleteCartItems(){
            //given
            Long userId = 1L;

            Cart cart = Cart.create(userId);
            cart.addItem(1L, 3);
            cart.addItem(2L, 3);
            cartRepository.save(cart);

            CartItem item1 = cart.findItemByProductVariantId(1L).orElseThrow();
            CartItem item2 = cart.findItemByProductVariantId(2L).orElseThrow();
            DeleteCartItemsCommand command = DeleteCartItemsCommand.of(userId, List.of(item1.getId(), item2.getId()));
            //when
            cartCommandService.deleteCartItems(command);
            //then
            Cart findCart = cartRepository.findByUserId(userId).orElseThrow();
            assertThat(findCart.getCartItems()).isEmpty();
        }

        @Test
        @DisplayName("장바구니를 찾을 수 없으면 예외가 발생한다")
        void deleteCartItems_notFound_cart(){
            //given
            DeleteCartItemsCommand command = Instancio.create(DeleteCartItemsCommand.class);
            //when
            //then
            assertThatThrownBy(() -> cartCommandService.deleteCartItems(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.CART_NOT_FOUND);
        }
    }
}