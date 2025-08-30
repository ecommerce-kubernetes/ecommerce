package com.example.order_service.service;

import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponseDto;
import com.example.order_service.dto.response.CartResponseDto;

public interface CartService {
    CartItemResponseDto addItem(Long userId, CartItemRequest cartItemRequest);
    CartResponseDto getCartItemList(Long userId);
    void deleteCartItemById(Long cartItemId);
    void deleteCartAll(Long cartId);
    void deleteCartItemByProductId(Long productId);
}
