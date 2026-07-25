package com.example.order_service.cart.domain;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

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

    private CartItem(Long productVariantId, int quantity) {
        Assert.notNull(productVariantId, "장바구니 항목 생성시 상품 변형 아이디는 필수입니다.");

        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static CartItem create(Long productVariantId, int quantity, int maxLimit) {
        if (quantity <= 0) {
            throw new BusinessException(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
        }

        if (maxLimit < quantity) {
            throw new BusinessException(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
        }

        return new CartItem(productVariantId, quantity);
    }

    public void addQuantity(int quantity, int maxLimit) {
        if (maxLimit < this.quantity + quantity) {
            throw new BusinessException(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
        }
        this.quantity = this.quantity + quantity;
    }

    public void updateQuantity(int quantity, int maxLimit) {
        if (quantity <= 0) {
            throw new BusinessException(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
        }

        if (maxLimit < quantity) {
            throw new BusinessException(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
        }

        this.quantity = quantity;
    }

    protected void setCart(Cart cart) {
        this.cart = cart;
    }
}
