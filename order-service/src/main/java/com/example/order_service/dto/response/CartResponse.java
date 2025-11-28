package com.example.order_service.dto.response;

import lombok.*;

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
}
