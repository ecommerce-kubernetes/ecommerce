package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.model.Cart;
import com.example.order_service.api.cart.domain.model.CartItem;
import com.example.order_service.api.cart.domain.repository.CartItemRepository;
import com.example.order_service.api.cart.domain.repository.CartRepository;
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
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartItemDto addItemToCart(Long userId, Long productVariantId, int quantity){
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.create(userId)));
        CartItem cartItem = cart.addItem(productVariantId, quantity);
        CartItem savedItem = cartItemRepository.save(cartItem);
        return CartItemDto.from(savedItem);
    }

    @Transactional(readOnly = true)
    public CartItemDto getCartItem(Long cartItemId){
        CartItem cartItem = getCartItemByCartItemId(cartItemId);
        return CartItemDto.from(cartItem);
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems(Long userId){
        return cartRepository.findWithItemsByUserId(userId)
                .map(this::createCartItemDtoList)
                .orElseGet(List::of);
    }

    public void deleteCartItem(Long userId, Long cartItemId){
        CartItem cartItem = getCartItemByCartItemId(cartItemId);

        if(!cartItem.getCart().isOwner(userId)){
            throw new BusinessException(CartErrorCode.CART_NO_PERMISSION);
        }
        cartItem.removeFromCart();
        cartItemRepository.delete(cartItem);
    }

    public void clearCart(Long userId){
        cartRepository.findWithItemsByUserId(userId)
                .ifPresent(Cart::clearItems);
    }

    public CartItemDto updateQuantity(Long cartItemId, int quantity){
        CartItem cartItem = getCartItemByCartItemId(cartItemId);
        cartItem.updateQuantity(quantity);
        return CartItemDto.from(cartItem);
    }

    public void deleteByProductVariantIds(Long userId, List<Long> productVariantIds) {
        Cart cart = getCartWithItemsByUserId(userId);
        cart.deleteItemByProductVariantIds(productVariantIds);
    }

    private List<CartItemDto> createCartItemDtoList(Cart cart){
        return cart.getCartItems().stream().map(CartItemDto::from)
                .toList();
    }

    private Cart getCartWithItemsByUserId(Long userId) {
        return cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
    }

    private CartItem getCartItemByCartItemId(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_ITEM_NOT_FOUND));
    }
}
