package com.example.order_service.cart.domain;

import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class CartTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("장바구니를 생성한다.")
    void create() {
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
    void create_userId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Cart.create(null, idGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 생성시 유저 아이디는 필수 입니다.");
    }

    @Test
    @DisplayName("장바구니를 생성할때 아이디 생성기가 누락되면 예외가 발생한다.")
    void create_idGenerator_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Cart.create(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 생성기는 필수 입니다.");
    }

    @Test
    @DisplayName("장바구니를 생성할때 아이디가 누락되면 예외가 발생한다.")
    void create_id_null() {
        //given
        IdGenerator nullIdGenerator = () -> null;
        //when
        //then
        assertThatThrownBy(() -> Cart.create(1L, nullIdGenerator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 생성시 장바구니 아이디는 필수 입니다.");
    }

    @Test
    @DisplayName("여러 항목을 장바구니에 추가한다")
    void addItems() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        AddCartItemsContext context = AddCartItemsContext.builder()
                .items(List.of(
                        AddCartItemsContext.Item.builder()
                                .productVariantId(1L)
                                .quantity(2)
                                .maxLimit(100)
                                .build(),
                        AddCartItemsContext.Item.builder()
                                .productVariantId(2L)
                                .quantity(3)
                                .maxLimit(100)
                                .build()
                ))
                .build();
        //when
        List<CartItem> cartItems = cart.addItems(context, idGenerator);
        //then
        assertThat(cart.getCartItems()).hasSize(2);
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems)
                .extracting("productVariantId", "quantity")
                .containsExactlyInAnyOrder(
                        tuple(1L, 2),
                        tuple(2L, 3)
                );
    }

    @Test
    @DisplayName("장바구니 상품 추가시 상품이 비어있으면 예외가 발생한다.")
    void addItems_items_null() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        AddCartItemsContext context = AddCartItemsContext.builder()
                .items(Collections.emptyList())
                .build();
        //when
        //then
        assertThatThrownBy(() -> cart.addItems(context, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_ITEMS_REQUIRED);
    }

    @Test
    @DisplayName("항목을 추가한다")
    void addItem() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        Long productVariantId = 1L;
        int quantity = 3;
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .maxLimit(100)
                .build();
        AddCartItemsContext context = AddCartItemsContext.builder()
                .items(List.of(itemCtx))
                .build();
        //when
        cart.addItems(context, idGenerator);
        //then
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems())
                .extracting("productVariantId", "quantity")
                .containsExactly(
                        tuple(productVariantId, quantity)
                );
    }

    @Test
    @DisplayName("동일한 항목이 있다면 기존 항목의 수량을 증가시킨다")
    void addItemWhenExist() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        Long productVariantId = 1L;
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(productVariantId)
                .quantity(3)
                .maxLimit(100)
                .build();
        AddCartItemsContext context = AddCartItemsContext.builder()
                .items(List.of(itemCtx))
                .build();
        cart.addItems(context, idGenerator);
        //when
        cart.addItems(context, idGenerator);
        //then
        assertThat(cart.getCartItems()).hasSize(1);
        assertThat(cart.getCartItems())
                .extracting("productVariantId", "quantity")
                .containsExactly(
                        tuple(productVariantId, 6)
                );
    }

    @Test
    @DisplayName("장바구니 최대 항목이 초과한 경우 예외가 발생한다")
    void addItemExceedCartSize() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        AddCartItemsContext maxContext = createMaxAddCartItemContext();
        cart.addItems(maxContext, idGenerator);

        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(100L)
                .quantity(3)
                .maxLimit(100)
                .build();
        AddCartItemsContext context = AddCartItemsContext.builder()
                .items(List.of(itemCtx))
                .build();
        //when
        //then
        assertThatThrownBy(() -> cart.addItems(context, idGenerator))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_SIZE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("항목 수량을 변경한다")
    void updateItemQuantity() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(3)
                .maxLimit(100)
                .build();
        AddCartItemsContext context = AddCartItemsContext.builder()
                .items(List.of(itemCtx))
                .build();
        cart.addItems(context, idGenerator);
        CartItem cartItem = cart.findItemByProductVariantId(1L).orElseThrow();

        UpdateCartItemContext updateContext = UpdateCartItemContext.builder()
                .cartItemId(cartItem.getId())
                .quantity(5)
                .maxLimit(100)
                .build();
        //when
        cart.updateItemQuantity(updateContext);
        //then
        CartItem item = cart.findItemByCartItemId(cartItem.getId()).orElseThrow();
        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("항목을 찾을 수 없으면 예외가 발생한다")
    void updateItemQuantity_notFound_cartItem() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        UpdateCartItemContext updateContext = UpdateCartItemContext.builder()
                .cartItemId(999L)
                .quantity(5)
                .maxLimit(100)
                .build();
        //when
        //then
        assertThatThrownBy(() -> cart.updateItemQuantity(updateContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("항목을 삭제한다")
    void deleteItem() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        AddCartItemsContext.Item itemCtx = AddCartItemsContext.Item.builder()
                .productVariantId(1L)
                .quantity(3)
                .maxLimit(100)
                .build();
        AddCartItemsContext context = AddCartItemsContext.builder()
                .items(List.of(itemCtx))
                .build();
        cart.addItems(context, idGenerator);
        CartItem cartItem = cart.findItemByProductVariantId(1L).orElseThrow();
        //when
        cart.deleteItem(cartItem.getId());
        //then
        assertThat(cart.getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("항목을 찾을 수 없으면 예외가 발생한다")
    void deleteItem_notFound_cartItem() {
        //given
        Cart cart = Cart.create(1L, idGenerator);
        //when
        //then
        assertThatThrownBy(() -> cart.deleteItem(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(CartErrorCode.CART_ITEM_NOT_FOUND);
    }

    private AddCartItemsContext createMaxAddCartItemContext() {
        List<AddCartItemsContext.Item> list = new ArrayList<>();
        for (long i = 0; i <20; i++) {
            AddCartItemsContext.Item item = AddCartItemsContext.Item.builder()
                    .productVariantId(i)
                    .quantity(3)
                    .maxLimit(100)
                    .build();
            list.add(item);
        }
        return AddCartItemsContext.builder()
                .items(list)
                .build();
    }
}
