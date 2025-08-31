package com.example.order_service.service;

import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;

public interface CartService {
    CartItemResponse addItem(Long userId, CartItemRequest cartItemRequest);
    CartResponse getCartItemList(Long userId);
    void deleteCartItemById(Long userId, Long cartItemId);
    void deleteCartAll(Long userId);
    void deleteCartItemByProductId(Long productId);
}
