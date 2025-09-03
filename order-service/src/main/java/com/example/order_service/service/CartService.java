package com.example.order_service.service;

import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService{
    private final ProductClientService productClientService;
    private final CartsRepository cartsRepository;

    public CartItemResponse addItem(Long userId, CartItemRequest request) {
        Carts cart = getCartByUserOrCreate(userId);

        ProductResponse productResponse;
        try{
            productResponse = productClientService.fetchProductByVariantId(request.getProductVariantId());
        } catch (NotFoundException ex){
            cart.getCartItems().stream()
                    .filter(ci -> ci.getProductVariantId().equals(request.getProductVariantId()))
                    .findFirst()
                    .ifPresent(cart::removeCartItem);
            throw ex;
        }
        CartItems savedCartItem;

        Optional<CartItems> item = cart.getCartItems().stream().filter(ci -> Objects.equals(ci.getProductVariantId(), request.getProductVariantId()))
                .findFirst();
        if(item.isPresent()){
            item.get().addQuantity(request.getQuantity());
            savedCartItem = item.get();
        } else {
            CartItems cartItem = new CartItems(productResponse.getProductVariantId(), request.getQuantity());
            cart.addCartItem(cartItem);
            savedCartItem = cartItem;
        }
        cartsRepository.save(cart);
        return createCartItemResponse(savedCartItem, productResponse);
    }

    private Carts getCartByUserOrCreate(Long userId){
        return cartsRepository.findByUserId(userId).orElseGet(() -> new Carts(userId));
    }

    public CartResponse getCartItemList(Long userId) {
        return null;
    }

    @Transactional(readOnly = true)
    public void deleteCartItemById(Long userId, Long cartItemId) {

    }

    public void deleteCartAll(Long cartId) {
    }

    public void deleteCartItemByProductId(Long productId) {
    }

    private CartItemResponse createCartItemResponse(CartItems cartItem, ProductResponse productResponse){
        return new CartItemResponse(cartItem.getId(), productResponse.getProductId(),
                cartItem.getProductVariantId(),
                productResponse.getProductName(),
                productResponse.getThumbnailUrl(),
                productResponse.getItemOptions(),
                productResponse.getPrice(),
                productResponse.getDiscountRate(),
                cartItem.getQuantity());
    }
}
