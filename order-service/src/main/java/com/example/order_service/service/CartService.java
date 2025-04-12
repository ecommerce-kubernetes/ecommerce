package com.example.order_service.service;

import com.example.order_service.dto.request.CartItemRequestDto;
import com.example.order_service.dto.response.CartItemResponseDto;
import com.example.order_service.dto.response.CartResponseDto;

import java.util.List;

public interface CartService {
    CartItemResponseDto addItem(Long userId, CartItemRequestDto cartItemRequestDto);
    CartResponseDto getCartItemList(Long userId);
    void deleteCartItemById(Long cartItemId);
    void deleteCartAll(Long cartId);
    void deleteCartItemByProductId(Long productId);
}
