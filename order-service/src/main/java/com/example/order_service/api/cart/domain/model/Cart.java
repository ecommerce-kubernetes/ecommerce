package com.example.order_service.api.cart.domain.model;

import com.example.order_service.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cart", cascade = CascadeType.PERSIST, orphanRemoval = true)
    List<CartItem> cartItems = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    public Cart(Long userId){
        this.userId = userId;
    }

    public void addCartItem(CartItem cartItem){
        cartItems.add(cartItem);
        cartItem.setCart(this);
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

    public CartItem addItem(Long productVariantId, int quantity){
        Optional<CartItem> existCartItem = this.cartItems.stream()
                .filter(item -> item.getProductVariantId().equals(productVariantId))
                .findFirst();

        if(existCartItem.isPresent()){
            existCartItem.get().addQuantity(quantity);
            return existCartItem.get();
        } else {
            CartItem cartItem = CartItem.create(productVariantId, quantity);
            this.cartItems.add(cartItem);
            cartItem.setCart(this);
            return cartItem;
        }
    }

    public void deleteItemByProductVariantIds(List<Long> productVariantIds) {
        List<CartItem> items = cartItems.stream()
                .filter(item -> productVariantIds.contains(item.getProductVariantId())).toList();
        items.forEach(item -> item.setCart(null));
        this.cartItems.removeAll(items);
    }

    public static Cart create(Long userId){
        return Cart.builder()
                .userId(userId)
                .build();
    }
}
