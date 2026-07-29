package com.example.order_service.cart.application.facade.mapper;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.domain.context.CreateCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.instancio.Select.field;

public class CartMapperTest {

    private final CartMapper cartMapper = Mappers.getMapper(CartMapper.class);

    @Test
    @DisplayName("장바구니 추가 context 매핑 테스트")
    void toCreateContext() {
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

        Map<Long, CartProductResult.CartProductDetail> productMap = Map.of(productVariantId, product);
        //when
        CreateCartItemsContext createContext = cartMapper.toCreateContext(command, productMap);
        //then
        assertThat(createContext.userId()).isEqualTo(userId);

        assertThat(createContext.items()).hasSize(1);
        assertThat(createContext.items())
                .extracting("productVariantId", "quantity", "maxLimit")
                .containsExactly(
                        tuple(productVariantId, quantity, product.stock())
                );
    }

    @Test
    @DisplayName("수량 변경 context 매핑 테스트")
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
        UpdateCartItemContext context = cartMapper.toUpdateContext(command, cartItem, product);
        //then
        assertThat(context)
                .extracting("userId", "cartItemId", "quantity", "maxLimit")
                .containsExactly(userId, cartItemId, updateQuantity, product.stock());
    }
}
