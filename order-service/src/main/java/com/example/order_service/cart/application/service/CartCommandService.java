package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.domain.model.Cart;
import com.example.order_service.cart.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    }

    public void deleteCartItems(Long userId, List<Long> cartItemIds) {

    }
}
