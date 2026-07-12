package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.domain.model.Cart;
import com.example.order_service.cart.domain.repository.CartRepository;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartCommandService {
    private final CartRepository cartRepository;

    public void addCartItems(AddCartItemsCommand command) {
        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseGet(() -> Cart.create(command.userId()));
        for (AddCartItemsCommand.Item item: command.items()) {
            cart.addItem(item.productVariantId(), item.quantity());
        }
        cartRepository.save(cart);
    }

    public void updateCartItemQuantity(UpdateCartItemQuantityCommand command) {
        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
        cart.updateItemQuantity(command.cartItemId(), command.quantity());
    }

    public void deleteCartItems(DeleteCartItemsCommand command) {
        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
        for(Long cartItemId: command.cartItemIds()) {
            cart.deleteItem(cartItemId);
        }
    }
}
