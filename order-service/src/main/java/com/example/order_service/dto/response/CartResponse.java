package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private List<CartItemResponse> cartItems;
    private long cartTotalPrice;

    public CartResponse(List<CartItemResponse> cartItems){
        this.cartItems = cartItems;
        this.cartTotalPrice = calculateTotalPrice();
    }

    private long calculateTotalPrice(){
        return cartItems.stream()
                .mapToLong(CartItemResponse::getItemTotalPrice)
                .sum();
    }
}
