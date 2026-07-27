package com.example.order_service.cart.domain;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Cart extends BaseEntity {

    @Id
    private Long id;

    private Long userId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private Cart(Long id, Long userId) {
        Assert.notNull(id, "장바구니 생성시 장바구니 아이디는 필수 입니다.");
        Assert.notNull(userId, "장바구니 생성시 유저 아이디는 필수입니다.");
        this.id = id;
        this.userId = userId;
    }

    public static Cart create(Long userId, IdGenerator idGenerator) {
        Long id = idGenerator.generate();
        return new Cart(id, userId);
    }

    public CartItem addItem(Long productVariantId, int quantity, int maxLimit, IdGenerator idGenerator) {
        if (this.cartItems.size() >= 20) {
            throw new BusinessException(CartErrorCode.CART_SIZE_LIMIT_EXCEEDED);
        }

        Optional<CartItem> existing = findItemByProductVariantId(productVariantId);

        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.addQuantity(quantity, maxLimit);
            return cartItem;
        }

        CartItem cartItem = CartItem.create(productVariantId, quantity, maxLimit, idGenerator);
        this.cartItems.add(cartItem);
        cartItem.setCart(this);

        return cartItem;
    }

    public Optional<CartItem> findItemByProductVariantId(Long productVariantId) {
        return cartItems.stream()
                .filter(item -> item.getProductVariantId().equals(productVariantId))
                .findFirst();
    }

    public Optional<CartItem> findItemByCartItemId(Long cartItemId) {
        return cartItems.stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst();
    }

    public void updateItemQuantity(Long cartItemId, Integer quantity, int maxLimit) {
        CartItem cartItem = findItemByCartItemId(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.updateQuantity(quantity, maxLimit);
    }

    public void deleteItem(Long cartItemId) {
        CartItem cartItem = findItemByCartItemId(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        cartItems.remove(cartItem);
        cartItem.setCart(null);
    }
}
