package com.example.order_service.service;

import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.CompactProductResponseDto;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartItemsRepository;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                    .filter(ci -> ci.getProductId().equals(request.getProductVariantId()))
                    .findFirst()
                    .ifPresent(cart::removeCartItem);
            throw ex;
        }
        Optional<CartItems> item = cart.getCartItems().stream().filter(ci -> Objects.equals(ci.getProductId(), request.getProductVariantId()))
                .findFirst();
        if(item.isPresent()){
            item.get().addQuantity(request.getQuantity());
        } else {
            CartItems cartItem = new CartItems(productResponse.getProductVariantId(), request.getQuantity());
            cart.addCartItem(cartItem);
        }
        cartsRepository.save(cart);
        return null;
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
}
