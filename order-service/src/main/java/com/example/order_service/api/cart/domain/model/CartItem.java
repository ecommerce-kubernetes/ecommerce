package com.example.order_service.api.cart.domain.model;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;
    private Long productVariantId;
    private int quantity;

    @Builder(access = AccessLevel.PRIVATE)
    public CartItem(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static CartItem create(Long productVariantId, int quantity){
        if (quantity <= 0) {
            throw new BusinessException(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED);
        }
        return CartItem.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    public void addQuantity(int quantity){
        this.quantity = this.quantity + quantity;
    }

    public void updateQuantity(int quantity) {
        if(quantity <= 0){
            throw new BusinessException(CartErrorCode.CART_ITEM_MINIMUM_ONE_REQUIRED);
        }
        this.quantity = quantity;
    }

    public void removeFromCart(){
        this.cart.getCartItems().remove(this);
        this.cart = null;
    }

    protected void setCart(Cart cart){
        this.cart = cart;
    }
}
