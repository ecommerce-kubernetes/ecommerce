package com.example.order_service.entity;

import com.example.order_service.entity.base.BaseEntity;
import com.example.order_service.service.client.dto.ProductResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CartItems> cartItems = new ArrayList<>();

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

    public CartItems addItem(ProductResponse productResponse, int quantity){
        Optional<CartItems> item = cartItems.stream().filter(ci -> productResponse.getProductVariantId().equals(ci.getProductVariantId()))
                .findFirst();

        if(item.isPresent()){
            CartItems existingItem = item.get();
            existingItem.addQuantity(quantity);
            return existingItem;
        } else {
            CartItems cartItem = new CartItems(productResponse.getProductVariantId(), quantity);
            addCartItem(cartItem);
            return cartItem;
        }
    }

    public void removeCartItem(CartItems cartItem){
        cartItems.remove(cartItem);
        cartItem.setCart(null);
    }
}
