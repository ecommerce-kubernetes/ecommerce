package com.example.order_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CartItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Carts cart;
    private Long productId;
    private int quantity;

    public CartItems(Carts cart, Long productId, int quantity){
        this.cart = cart;
        this.productId = productId;
        this.quantity = quantity;
        cart.getCartItems().add(this);
    }

    public void addQuantity(int quantity){
        this.quantity = this.quantity + quantity;
    }
}
