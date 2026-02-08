package com.example.order_service.api.support.fixture.cart;

import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse.CartItemOption;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse.CartItemPrice;
import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;

import java.util.List;

public class CartResponseFixture {

    public static CartItemResponse.CartItemResponseBuilder anCartItemResponse() {
        return CartItemResponse.builder()
                .id(1L)
                .status(CartItemStatus.AVAILABLE)
                .isAvailable(true)
                .productId(1L)
                .productVariantId(1L)
                .productName("상품")
                .thumbnailUrl("http://thumbnail.jpg")
                .quantity(1)
                .price(anCartItemPrice().build())
                .lineTotal(9000L)
                .options(List.of(anCartItemOption().build()));
    }

    public static CartItemPrice.CartItemPriceBuilder anCartItemPrice() {
        return CartItemPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L);
    }

    public static CartItemOption.CartItemOptionBuilder anCartItemOption() {
        return CartItemOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }
}
