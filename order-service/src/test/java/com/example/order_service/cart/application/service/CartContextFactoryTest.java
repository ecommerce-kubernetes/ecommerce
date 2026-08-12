package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.service.dto.data.CartItemData;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.instancio.Select.field;

class CartContextFactoryTest {

    private final CartContextFactory cartContextFactory = new CartContextFactory();

    @Test
    @DisplayName("장바구니 상품 추가 컨텍스트를 생성한다.")
    void toAddCartItemsContext() {
        //given
        Long userId = 1L;
        Long productVariantId = 1L;
        int quantity = 3;
        AddCartItemsCommand.Item item = AddCartItemsCommand.Item.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();

        AddCartItemsCommand command = AddCartItemsCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        CartProductResult.CartProductDetail product = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), productVariantId)
                .create();

        CartProductResult products = CartProductResult.builder()
                .products(List.of(product))
                .build();
        //when
        AddCartItemsContext context = cartContextFactory.toAddCartItemsContext(command, products);
        //then
        assertThat(context.items()).hasSize(1);
        assertThat(context.items())
                .extracting("productVariantId", "quantity", "maxLimit")
                .containsExactly(
                        tuple(productVariantId, quantity, product.stock())
                );
    }

    @Test
    @DisplayName("수량 변경 컨텍스트를 생성한다.")
    void toUpdateContext() {
        //given
        Long cartItemId = 1L;
        Long productVariantId = 1L;
        Long userId = 1L;
        int updateQuantity = 5;
        UpdateCartItemQuantityCommand command = UpdateCartItemQuantityCommand.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .quantity(updateQuantity)
                .build();

        CartItemData cartItem = CartItemData.builder()
                .cartItemId(cartItemId)
                .productVariantId(productVariantId)
                .quantity(3)
                .build();

        CartProductResult.CartProductDetail product = Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), productVariantId)
                .create();
        //when
        UpdateCartItemContext context = cartContextFactory.toUpdateContext(command, cartItem, product);
        //then
        assertThat(context)
                .extracting( "cartItemId", "quantity", "maxLimit")
                .containsExactly(cartItemId, updateQuantity, product.stock());
    }
}