package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.service.dto.data.CartItemData;
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
        Long productVariantId = 1L;
        int commandQuantity = 3;
        int stock = 100;

        AddCartItemsCommand command = createAddCommand(productVariantId, commandQuantity);
        CartProductResult products = createProductResult(productVariantId, stock);
        //when
        AddCartItemsContext context = cartContextFactory.toAddCartItemsContext(command, products);
        //then
        assertThat(context.items()).hasSize(1);
        assertThat(context.items())
                .extracting("productVariantId", "quantity", "maxLimit")
                .containsExactly(
                        tuple(productVariantId, commandQuantity, stock)
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
        UpdateCartItemQuantityCommand command = createUpdateQuantityCommand(userId, cartItemId, updateQuantity);

        CartItemData cartItem = createCartItemData(cartItemId, productVariantId, 3);

        CartProductResult.CartProductDetail product = createProductDetail(productVariantId, 100);
        //when
        UpdateCartItemContext context = cartContextFactory.toUpdateContext(command, cartItem, product);
        //then
        assertThat(context)
                .extracting( "cartItemId", "quantity", "maxLimit")
                .containsExactly(cartItemId, updateQuantity, product.stock());
    }

    private AddCartItemsCommand createAddCommand(Long productVariantId, int quantity) {
        return AddCartItemsCommand.builder()
                .userId(1L)
                .items(List.of(AddCartItemsCommand.Item.builder()
                        .productVariantId(productVariantId)
                        .quantity(quantity)
                        .build()))
                .build();
    }

    private UpdateCartItemQuantityCommand createUpdateQuantityCommand(Long userId, Long cartItemId, int updateQuantity) {
        return UpdateCartItemQuantityCommand.builder()
                .userId(userId)
                .cartItemId(cartItemId)
                .quantity(updateQuantity)
                .build();
    }

    private CartItemData createCartItemData(Long cartItemId, Long productVariantId, int quantity) {
        return CartItemData.builder()
                .cartItemId(cartItemId)
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    private CartProductResult createProductResult(Long productVariantId, int stock) {
        CartProductResult.CartProductDetail detail = createProductDetail(productVariantId, stock);
        return CartProductResult.builder().products(List.of(detail)).build();
    }

    private CartProductResult.CartProductDetail createProductDetail(Long productVariantId, int stock) {
        return Instancio.of(CartProductResult.CartProductDetail.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("stock"), stock)
                .create();
    }
}