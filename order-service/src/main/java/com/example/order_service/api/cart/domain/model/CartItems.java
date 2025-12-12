package com.example.order_service.api.cart.domain.model;

import com.example.order_service.api.common.exception.InvalidQuantityException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private Long productVariantId;
    private int quantity;

    @Builder
    public CartItems(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static CartItems of(Long productVariantId, int quantity){
        return CartItems.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    public void addQuantity(int quantity){
        this.quantity = this.quantity + quantity;
    }

    public void updateQuantity(int quantity) {
        if(quantity <= 0){
            throw new InvalidQuantityException("상품 수량은 1 이상이여야 합니다");
        }
        this.quantity = quantity;
    }

    public void removeFromCart(){
        this.cart.getCartItems().remove(this);
        this.cart = null;
    }

    protected void setCart(Carts cart){
        this.cart = cart;
    }
}
