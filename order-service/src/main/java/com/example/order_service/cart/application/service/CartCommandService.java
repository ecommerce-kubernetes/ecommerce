package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.port.CartRepository;
import com.example.order_service.cart.domain.Cart;
import com.example.order_service.cart.domain.CartItem;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartCommandService {
    private final CartRepository cartRepository;
    private final IdGenerator idGenerator;

    public List<Long> addCartItems(Long userId, AddCartItemsContext context) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.create(userId, idGenerator));

        List<CartItem> addedItems = cart.addItems(context, idGenerator);
        cartRepository.save(cart);
        return addedItems.stream()
                .map(CartItem::getId)
                .toList();
    }

    public void updateCartItemQuantity(Long userId, UpdateCartItemContext context) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
        cart.updateItemQuantity(context);
    }

    public void deleteCartItems(Long userId, List<Long> cartItemIds) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
        for(Long cartItemId: cartItemIds) {
            cart.deleteItem(cartItemId);
        }
    }
}
