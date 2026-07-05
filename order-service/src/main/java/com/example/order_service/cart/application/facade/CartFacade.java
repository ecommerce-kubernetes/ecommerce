package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.service.CartService;
import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.CartCommand;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.dto.result.CartResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartFacade {
    private final CartService cartService;
    private final CartProductGateway cartProductGateway;

    public CartResult addItems(AddCartItemsCommand command) {
        return null;
    }

    public CartResult getCartDetails(Long userId){
        return null;
    }

    public CartResult updateCartItemQuantity(CartCommand.UpdateQuantity command){
        return null;
    }

    public void removeCartItems(Long userId, List<Long> cartItemIds){
        cartService.deleteCartItems(userId, cartItemIds);
    }

    public void removePurchasedItems(Long userId, List<Long> productVariantIds) {
        cartService.deleteByProductVariantIds(userId, productVariantIds);
    }
}
