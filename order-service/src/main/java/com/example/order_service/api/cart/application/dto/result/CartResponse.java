package com.example.order_service.api.cart.application.dto.result;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CartResponse {
    private List<CartItemResponse> cartItems;
    private long cartTotalPrice;

    @Builder
    private CartResponse(List<CartItemResponse> cartItems, long cartTotalPrice){
        this.cartItems = cartItems;
        this.cartTotalPrice = cartTotalPrice;
    }

    public static CartResponse ofEmpty(){
        return CartResponse.builder()
                .cartItems(List.of())
                .cartTotalPrice(0)
                .build();
    }

    public static CartResponse from(List<CartItemResponse> cartItems){
        long total = cartItems.stream()
                .mapToLong(CartItemResponse::getLineTotal)
                .sum();

        return of(cartItems, total);
    }

    public static CartResponse of(List<CartItemResponse> cartItems, long cartTotalPrice){
        return CartResponse.builder()
                .cartItems(cartItems)
                .cartTotalPrice(cartTotalPrice)
                .build();
    }
}
