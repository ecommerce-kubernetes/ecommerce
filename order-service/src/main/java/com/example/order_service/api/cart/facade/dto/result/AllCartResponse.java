package com.example.order_service.api.cart.facade.dto.result;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AllCartResponse {
    private List<CartItemResponse> cartItems;
    private long cartTotalPrice;

    @Builder
    private AllCartResponse(List<CartItemResponse> cartItems, long cartTotalPrice){
        this.cartItems = cartItems;
        this.cartTotalPrice = cartTotalPrice;
    }

    public static AllCartResponse empty(){
        return AllCartResponse.builder()
                .cartItems(List.of())
                .cartTotalPrice(0)
                .build();
    }

    public static AllCartResponse from(List<CartItemResponse> cartItems){
        long total = cartItems.stream()
                .filter(CartItemResponse::isAvailable)
                .mapToLong(CartItemResponse::getLineTotal)
                .sum();

        return AllCartResponse.builder()
                .cartItems(cartItems)
                .cartTotalPrice(total)
                .build();
    }
}
