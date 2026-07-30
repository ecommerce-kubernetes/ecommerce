package com.example.order_service.cart.domain;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    private Long productVariantId;

    private int quantity;

    private CartItem(Long id, Long productVariantId, int quantity) {
        this.id = id;
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public static CartItem create(Long productVariantId, int quantity, int maxLimit, IdGenerator idGenerator) {
        Assert.notNull(productVariantId, "장바구니 항목 생성시 상품 변형 아이디는 필수입니다.");
        Assert.notNull(idGenerator, "아이디 생성기는 필수 입니다.");

        Long id = idGenerator.generate();

        Assert.notNull(id, "장바구니 항목 생성시 식별자는 필수입니다.");

        if (quantity <= 0) {
            throw new BusinessException(CartErrorCode.INVALID_CART_ITEM_QUANTITY);
        }

        if (maxLimit < quantity) {
            throw new BusinessException(CartErrorCode.QUANTITY_EXCEED_MAX_LIMIT);
        }

        return new CartItem(id, productVariantId, quantity);
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
