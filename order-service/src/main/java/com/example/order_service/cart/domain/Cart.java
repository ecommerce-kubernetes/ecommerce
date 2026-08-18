package com.example.order_service.cart.domain;

import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
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
        this.id = id;
        this.userId = userId;
    }

    public static Cart create(Long userId, IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "아이디 생성기는 필수 입니다.");
        Assert.notNull(userId, "장바구니 생성시 유저 아이디는 필수 입니다.");

        Long id = idGenerator.generate();
        Assert.notNull(id, "장바구니 생성시 장바구니 아이디는 필수 입니다.");

        return new Cart(id, userId);
    }

    public List<CartItem> addItems(AddCartItemsContext context, IdGenerator idGenerator) {
        if (context.items().isEmpty()) {
            throw new BusinessException(CartErrorCode.CART_ITEMS_REQUIRED);
        }
        return context.items().stream()
                .map(item -> addItem(item, idGenerator))
                .toList();
    }

    private CartItem addItem(AddCartItemsContext.Item itemCtx, IdGenerator idGenerator) {
        Optional<CartItem> existing = findItemByProductVariantId(itemCtx.productVariantId());

        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.addQuantity(itemCtx.quantity(), itemCtx.maxLimit());
            return cartItem;
        }

        if (this.cartItems.size() >= 20) {
            throw new BusinessException(CartErrorCode.CART_SIZE_LIMIT_EXCEEDED);
        }

        CartItem cartItem = CartItem.create(itemCtx, idGenerator);
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

    public void updateItemQuantity(UpdateCartItemContext context) {
        CartItem cartItem = findItemByCartItemId(context.cartItemId())
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        cartItem.updateQuantity(context.quantity(), context.maxLimit());
    }

    public void deleteItem(Long cartItemId) {
        CartItem cartItem = findItemByCartItemId(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
        cartItems.remove(cartItem);
        cartItem.setCart(null);
    }
}
