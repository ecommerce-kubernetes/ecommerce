package com.example.order_service.cart.domain.model;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    private CartItem(Long productVariantId, int quantity){
        this.productVariantId = Objects.requireNonNull(productVariantId, "장바구니 항목 생성시 상품 변형 아이디는 필수입니다.");
        this.quantity = quantity;
    }

    public static CartItem create(Long productVariantId, int quantity){
        if (quantity <= 0) {
            throw new BusinessException(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
        }
        return new CartItem(productVariantId, quantity);
    }

    public void addQuantity(int quantity){
        this.quantity = this.quantity + quantity;
    }

    public void updateQuantity(int quantity) {
        if(quantity <= 0){
            throw new BusinessException(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
        }
        this.quantity = quantity;
    }

    protected void setCart(Cart cart){
        this.cart = cart;
    }
}
