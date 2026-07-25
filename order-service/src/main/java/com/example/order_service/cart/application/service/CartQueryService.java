package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.port.CartRepository;
import com.example.order_service.cart.domain.Cart;
import com.example.order_service.cart.domain.CartItem;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CartQueryService {

    private final CartRepository cartRepository;

    public List<CartItemData> findCartItems(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cart.getCartItems().stream().map(CartItemData::from)
                        .toList())
                .orElse(Collections.emptyList());
    }

    public List<CartItemData> findCartItemsByVariantIds(Long userId, List<Long> productVariantIds) {
        return cartRepository.findByUserId(userId)
                .map(cart -> productVariantIds.stream()
                        .flatMap(id -> cart.findItemByProductVariantId(id).stream())
                        .map(CartItemData::from)
                        .toList())
                .orElse(Collections.emptyList());
    }

    public CartItemData getCartItem(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));

        CartItem cartItem = cart.findItemByCartItemId(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));

        return CartItemData.from(cartItem);
    }
}
