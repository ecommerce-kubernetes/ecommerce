package com.example.order_service.cart.domain;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.exception.BusinessException;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private Cart(Long userId) {
        Assert.notNull(userId, "장바구니 생성시 유저 아이디는 필수입니다.");
        this.userId = userId;
    }

    public static Cart create(Long userId) {
        return new Cart(userId);
    }

    public void addItem(Long productVariantId, int quantity) {
        if (this.cartItems.size() >= 20) {
            throw new BusinessException(CartErrorCode.CART_SIZE_LIMIT_EXCEEDED);
        }

        Optional<CartItem> existing = findItemByProductVariantId(productVariantId);

        if (existing.isPresent()) {
            existing.get().addQuantity(quantity, 100);
            return;
        }

        CartItem cartItem = CartItem.create(productVariantId, quantity, 100);
        this.cartItems.add(cartItem);
        cartItem.setCart(this);
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

    public void updateItemQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = findItemByCartItemId(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.updateQuantity(quantity);
    }

    public void deleteItem(Long cartItemId) {
        CartItem cartItem = findItemByCartItemId(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        cartItems.remove(cartItem);
        cartItem.setCart(null);
    }
}
