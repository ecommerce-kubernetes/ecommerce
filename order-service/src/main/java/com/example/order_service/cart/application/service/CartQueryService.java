package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.domain.repository.CartRepository;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
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

    public List<CartItemData> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(cart -> cart.getCartItems().stream().map(CartItemData::from)
                        .toList())
                .orElse(Collections.emptyList());
    }

    public List<CartItemData> getCartItems(Long userId, List<Long> productVariantIds) {
        return null;
    }

    public CartItemData getCartItem(Long cartItemId) {
        return null;
    }
}
