package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.model.CartItems;
import com.example.order_service.api.cart.domain.model.Carts;
import com.example.order_service.api.cart.domain.repository.CartItemsRepository;
import com.example.order_service.api.cart.domain.repository.CartsRepository;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.NotFoundException;
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
        Carts cart = cartsRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> cartsRepository.save(Carts.of(userId)));
        CartItems cartItem = cart.addItem(productVariantId, quantity);
        CartItems savedItem = cartItemsRepository.save(cartItem);
        return CartItemDto.of(savedItem);
    }

    @Transactional(readOnly = true)
    public CartItemDto getCartItem(Long cartItemId){
        CartItems cartItem = cartItemsRepository.findWithCartById(cartItemId)
                .orElseThrow(() -> new NotFoundException("장바구니에서 해당 상품을 찾을 수 없습니다"));
        return CartItemDto.of(cartItem);
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems(Long userId){
        return cartsRepository.findWithItemsByUserId(userId)
                .map(this::createCartItemDtoList)
                .orElseGet(List::of);
    }

    public void deleteCartItem(Long userId, Long cartItemId){
        CartItems cartItem = cartItemsRepository.findWithCartById(cartItemId)
                .orElseThrow(() -> new NotFoundException("장바구니에서 해당 상품을 찾을 수 없습니다"));

        Long cartUserId = cartItem.getCart().getUserId();
        if(!userId.equals(cartUserId)){
            throw new NoPermissionException("장바구니의 상품을 삭제할 권한이 없습니다");
        }
        cartItem.removeFromCart();
        cartItemsRepository.delete(cartItem);
    }

    public void clearCart(Long userId){
        cartsRepository.findWithItemsByUserId(userId)
                .ifPresent(Carts::clearItems);
    }

    public CartItemDto updateQuantity(Long cartItemId, int quantity){
        CartItems cartItem = cartItemsRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("장바구니에서 해당 상품을 찾을 수 없습니다"));

        cartItem.updateQuantity(quantity);
        return CartItemDto.of(cartItem);
    }

    public void deleteByProductVariantIds(Long userId, List<Long> productVariantIds) {
        Carts cart = cartsRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new NotFoundException("해당 유저의 장바구니를 찾을 수 없습니다"));

        cart.deleteItemByProductVariantIds(productVariantIds);
    }

    private List<CartItemDto> createCartItemDtoList(Carts cart){
        return cart.getCartItems().stream().map(CartItemDto::of)
                .toList();
    }
}
