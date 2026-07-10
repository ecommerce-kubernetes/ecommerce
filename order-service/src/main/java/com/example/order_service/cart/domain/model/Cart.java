package com.example.order_service.cart.domain.model;

import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.exception.application.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    private Cart(Long userId){
        this.userId = Objects.requireNonNull(userId, "장바구니 생성시 유저 아이디는 필수입니다.");
    }

    public static Cart create(Long userId){
        return new Cart(userId);
    }

    public void addItem(Long productVariantId, int quantity){
        if (this.cartItems.size() >= 20) {
            throw new BusinessException(CartErrorCode.EXCEED_AVAILABLE_CART_SIZE);
        }

        CartItem existing = findItem(productVariantId);

        if(existing != null) {
            existing.addQuantity(quantity);
            return;
        }

        CartItem cartItem = CartItem.create(productVariantId, quantity);
        this.cartItems.add(cartItem);
        cartItem.setCart(this);
    }

    public CartItem findItem(Long productVariantId) {
        return cartItems.stream()
                .filter(item -> item.getProductVariantId().equals(productVariantId))
                .findFirst()
                .orElse(null);
    }

    public Optional<CartItem> findItemByCartItemId(Long cartItemId) {
        return cartItems.stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst();
    }

    public void clearItems(){
        for (CartItem cartItem : cartItems) {
            cartItem.setCart(null);
        }
        cartItems.clear();
    }

    public boolean isOwner(Long accessUserId) {
        return this.userId.equals(accessUserId);
    }
}
