package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.fixture.CartCommandFixture;
import com.example.order_service.cart.application.fixture.CartDataFixture;
import com.example.order_service.cart.application.fixture.CartProductFixture;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.service.dto.data.CartItemData;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class CartContextFactoryTest {

    private final CartContextFactory cartContextFactory = new CartContextFactory();

    @Test
    @DisplayName("장바구니 상품 추가 컨텍스트를 생성한다.")
    void toAddCartItemsContext() {
        //given
        int commandQuantity = 3;
        int stock = 100;

        AddCartItemsCommand.Item item = CartCommandFixture.anItem().quantity(commandQuantity).build();
        AddCartItemsCommand command = CartCommandFixture.anAddCommand().items(List.of(item)).build();
        CartProductResult.CartProductDetail product = CartProductFixture.anProduct().stock(stock).build();
        CartProductResult productResult = CartProductFixture.anProducts().products(List.of(product)).build();

        //when
        AddCartItemsContext context = cartContextFactory.toAddCartItemsContext(command, productResult);
        //then
        assertThat(context.items()).hasSize(1);
        assertThat(context.items())
                .extracting("productVariantId", "quantity", "maxLimit")
                .containsExactly(
                        tuple(item.productVariantId(), commandQuantity, stock)
                );
    }

    @Test
    @DisplayName("수량 변경 컨텍스트를 생성한다.")
    void toUpdateContext() {
        //given
        Long cartItemId = 1L;
        int updateQuantity = 5;

        UpdateCartItemQuantityCommand command = CartCommandFixture.anUpdateQuantityCommand()
                .cartItemId(cartItemId).quantity(updateQuantity).build();

        CartItemData cartItem = CartDataFixture.anCartItemData().cartItemId(cartItemId).quantity(3).build();

        CartProductResult.CartProductDetail product = CartProductFixture.anProduct().stock(100).build();
        //when
        UpdateCartItemContext context = cartContextFactory.toUpdateContext(command, cartItem, product);
        //then
        assertThat(context)
                .extracting( "cartItemId", "quantity", "maxLimit")
                .containsExactly(cartItemId, updateQuantity, product.stock());
    }
}