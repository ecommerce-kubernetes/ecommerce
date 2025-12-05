package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.controller.dto.response.CartItemResponse;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartApplicationService {
    private final CartService cartService;
    private final ProductClientService productClientService;

    public CartItemResponse addItem(AddCartItemDto dto) {
        ProductResponse product = productClientService.fetchProductByVariantId(dto.getProductVariantId());
        Long userId = dto.getUserPrincipal().getUserId();
        CartItemDto result = cartService.addItemToCart(userId, dto.getProductVariantId(), dto.getQuantity());
        return CartItemResponse.of(result, product);
    }
}
