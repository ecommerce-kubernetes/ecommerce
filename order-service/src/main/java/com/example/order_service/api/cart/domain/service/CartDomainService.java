package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.model.Cart;
import com.example.order_service.api.cart.domain.model.CartItem;
import com.example.order_service.api.cart.domain.repository.CartItemsRepository;
import com.example.order_service.api.cart.domain.repository.CartsRepository;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartDomainService {
    private final CartsRepository cartsRepository;
    private final CartItemsRepository cartItemsRepository;

    public CartItemDto addItemToCart(Long userId, Long productVariantId, int quantity){
        Cart cart = cartsRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> cartsRepository.save(Cart.of(userId)));
        CartItem cartItem = cart.addItem(productVariantId, quantity);
        CartItem savedItem = cartItemsRepository.save(cartItem);
        return CartItemDto.of(savedItem);
    }

    @Transactional(readOnly = true)
    public CartItemDto getCartItem(Long cartItemId){
        CartItem cartItem = getCartItemByCartItemId(cartItemId);
        return CartItemDto.of(cartItem);
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems(Long userId){
        return cartsRepository.findWithItemsByUserId(userId)
                .map(this::createCartItemDtoList)
                .orElseGet(List::of);
    }

    public void deleteCartItem(Long userId, Long cartItemId){
        CartItem cartItem = getCartItemByCartItemId(cartItemId);

        if(!cartItem.getCart().isOwner(userId)){
            throw new BusinessException(CartErrorCode.CART_NO_PERMISSION);
        }
        cartItem.removeFromCart();
        cartItemsRepository.delete(cartItem);
    }

    public void clearCart(Long userId){
        cartsRepository.findWithItemsByUserId(userId)
                .ifPresent(Cart::clearItems);
    }

    public CartItemDto updateQuantity(Long cartItemId, int quantity){
        CartItem cartItem = getCartItemByCartItemId(cartItemId);
        cartItem.updateQuantity(quantity);
        return CartItemDto.of(cartItem);
    }

    public void deleteByProductVariantIds(Long userId, List<Long> productVariantIds) {
        Cart cart = getCartWithItemsByUserId(userId);

        cart.deleteItemByProductVariantIds(productVariantIds);
    }

    private List<CartItemDto> createCartItemDtoList(Cart cart){
        return cart.getCartItems().stream().map(CartItemDto::of)
                .toList();
    }

    private Cart getCartWithItemsByUserId(Long userId) {
        return cartsRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
    }

    private CartItem getCartItemByCartItemId(Long cartItemId) {
        return cartItemsRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
    }
}
