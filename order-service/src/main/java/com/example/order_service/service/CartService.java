package com.example.order_service.service;

import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.entity.Carts;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService{
    private final ProductClientService productClientService;
    private final CartsRepository cartsRepository;

    public CartItemResponse addItem(Long userId, CartItemRequest request) {
       return null;
    }

    public CartResponse getCartItemList(Long userId) {
        return null;
    }

    @Transactional(readOnly = true)
    public void deleteCartItemById(Long userId, Long cartItemId) {

    }

    public void clearAllCartItems(Long userId) {
        Optional<Carts> cart = cartsRepository.findByUserId(userId);
        cart.ifPresent(Carts::clearItems);
    }
}
