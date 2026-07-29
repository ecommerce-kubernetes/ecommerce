package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.domain.context.CreateCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import com.example.order_service.cart.application.port.CartRepository;
import com.example.order_service.cart.domain.Cart;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartCommandService {
    private final CartRepository cartRepository;
    private final IdGenerator idGenerator;

    public List<CartItemData> addCartItems(CreateCartItemsContext context) {
        Cart cart = cartRepository.findByUserId(context.userId())
                .orElseGet(() -> Cart.create(context.userId(), idGenerator));

        List<CartItemData> result = context.items().stream()
                .map(item -> cart.addItem(item.productVariantId(), item.quantity(), item.maxLimit(), idGenerator))
                .map(CartItemData::from)
                .toList();
        cartRepository.save(cart);
        return result;
    }

    public void updateCartItemQuantity(UpdateCartItemContext context) {
        Cart cart = cartRepository.findByUserId(context.userId())
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
        cart.updateItemQuantity(context.cartItemId(), context.quantity(), context.maxLimit());
    }

    public void deleteCartItems(Long userId, List<Long> cartItemIds) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
        for(Long cartItemId: cartItemIds) {
            cart.deleteItem(cartItemId);
        }
    }
}
