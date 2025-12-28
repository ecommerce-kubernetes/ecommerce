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
public class Carts extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cart", cascade = CascadeType.PERSIST, orphanRemoval = true)
    List<CartItems> cartItems = new ArrayList<>();

    @Builder
    public Carts(Long userId){
        this.userId = userId;
    }

    public void addCartItem(CartItems cartItem){
        cartItems.add(cartItem);
        cartItem.setCart(this);
    }

    public void clearItems(){
        for (CartItems cartItem : cartItems) {
            cartItem.setCart(null);
        }
        cartItems.clear();
    }

    public CartItems addItem(Long productVariantId, int quantity){
        Optional<CartItems> existCartItem = this.cartItems.stream()
                .filter(item -> item.getProductVariantId().equals(productVariantId))
                .findFirst();

        if(existCartItem.isPresent()){
            existCartItem.get().addQuantity(quantity);
            return  existCartItem.get();
        } else {
            CartItems cartItem = CartItems.of(productVariantId, quantity);
            this.cartItems.add(cartItem);
            cartItem.setCart(this);
            return cartItem;
        }
    }

    public void deleteItemByProductVariantIds(List<Long> productVariantIds) {
        List<CartItems> items = cartItems.stream()
                .filter(item -> productVariantIds.contains(item.getProductVariantId())).toList();
        items.forEach(item -> item.setCart(null));
        this.cartItems.removeAll(items);
    }

    public static Carts of(Long userId){
        return Carts.builder()
                .userId(userId)
                .build();
    }
}
