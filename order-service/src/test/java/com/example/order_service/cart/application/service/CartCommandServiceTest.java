package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.port.CartRepository;
import com.example.order_service.cart.domain.Cart;
import com.example.order_service.cart.domain.CartItem;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.support.annotation.IsolatedTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
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
    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("장바구니가 존재하지 않는 경우 장바구니를 생성한 뒤 상품을 추가한다")
    void addCartItems_whenNotExistCart_thenCreateCartAndAddItems() {
        //given
        Long userId = 1L;
        AddCartItemsContext context = createAddContext(1L, 3, 100);
        //when
        List<Long> cartItemIds = cartCommandService.addCartItems(userId, context);
        flushAndClear();
        //then
        Cart findCart = cartRepository.findByUserId(userId).orElseThrow();

        assertThat(findCart.getCartItems())
                .hasSize(1)
                .extracting("productVariantId", "quantity")
                .containsExactly(tuple(1L, 3));

        assertThat(cartItemIds)
                .containsExactlyElementsOf(
                        findCart.getCartItems().stream()
                                .map(CartItem::getId)
                                .toList()
                );
    }

    @Test
    @DisplayName("장바구니가 존재하는 경우 기존 장바구니에 상품을 추가한다")
    void addCartItems_whenExistCart_thenAddItemToExistCart() {
        //given
        Long userId = 1L;
        Cart savedCart = cartRepository.save(Cart.create(userId, idGenerator));
        flushAndClear();

        AddCartItemsContext context = createAddContext(1L, 3, 100);
        //when
        cartCommandService.addCartItems(userId, context);
        flushAndClear();
        //then
        Cart findCart = cartRepository.findByUserId(userId).orElseThrow();

        assertThat(findCart.getId()).isEqualTo(savedCart.getId());

        assertThat(findCart.getCartItems())
                .hasSize(1)
                .extracting("productVariantId", "quantity")
                .containsExactly(tuple(1L, 3));
    }

    @Test
    @DisplayName("장바구니 항목 수량을 변경한다")
    void updateCartItemQuantity() {
        //given
        Long userId = 1L;
        Cart cart = Cart.create(userId, idGenerator);
        cart.addItems(createAddContext(1L, 3, 100), idGenerator);
        cartRepository.save(cart);

        Long cartItemId = cart.findItemByProductVariantId(1L).orElseThrow().getId();

        flushAndClear();

        UpdateCartItemContext updateContext = UpdateCartItemContext.builder()
                .cartItemId(cartItemId)
                .quantity(2)
                .maxLimit(100)
                .build();
        //when
        cartCommandService.updateCartItemQuantity(userId, updateContext);
        flushAndClear();
        //then
        Cart findCart = cartRepository.findByUserId(userId).orElseThrow();
        CartItem findItem = findCart.findItemByCartItemId(cartItemId).orElseThrow();
        assertThat(findItem.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("장바구니를 찾을 수 없으면 예외가 발생한다")
    void updateCartItemQuantity_whenNotFoundCart_thenThrownException() {
        //given
        Long userId = 1L;
        UpdateCartItemContext context = UpdateCartItemContext.builder()
                .cartItemId(1L)
                .quantity(3)
                .maxLimit(100)
                .build();
        //when
        //then
        assertThatThrownBy(() -> cartCommandService.updateCartItemQuantity(userId, context))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니에 담긴 항목을 제거한다")
    void deleteCartItems() {
        //given
        Long userId = 1L;
        Cart cart = Cart.create(userId, idGenerator);
        cart.addItems(createAddContext(1L, 3, 100), idGenerator);
        cart.addItems(createAddContext(2L, 3, 100), idGenerator);
        cartRepository.save(cart);

        Long cartItemId1 = cart.findItemByProductVariantId(1L).orElseThrow().getId();
        Long cartItemId2 = cart.findItemByProductVariantId(2L).orElseThrow().getId();

        flushAndClear();
        //when
        cartCommandService.deleteCartItems(userId, List.of(cartItemId1, cartItemId2));
        flushAndClear();
        //then
        Cart findCart = cartRepository.findByUserId(userId).orElseThrow();
        assertThat(findCart.getCartItems()).isEmpty();
    }

    @Test
    @DisplayName("장바구니를 찾을 수 없으면 예외가 발생한다")
    void deleteCartItems_whenNotFoundCart_thenThrownException() {
        //given
        Long userId = 1L;
        List<Long> cartItemIds = List.of(1L, 2L);
        //when
        //then
        assertThatThrownBy(() -> cartCommandService.deleteCartItems(userId, cartItemIds))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_NOT_FOUND);
    }

    private AddCartItemsContext createAddContext(Long productVariantId, int quantity, int maxLimit) {
        AddCartItemsContext.Item item = AddCartItemsContext.Item.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .maxLimit(maxLimit)
                .build();
        return AddCartItemsContext.builder()
                .items(List.of(item))
                .build();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}