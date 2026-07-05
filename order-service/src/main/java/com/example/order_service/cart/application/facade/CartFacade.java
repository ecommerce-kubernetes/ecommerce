package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.dto.result.CartItemResult;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartService;
import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.CartCommand;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.dto.result.CartResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartFacade {
    private final CartService cartService;
    private final CartCommandService cartCommandService;
    private final CartProductGateway cartProductGateway;

    public CartResult addItems(AddCartItemsCommand command) {
        List<Long> productVariantIds = command.toProductVariantIds();
        CartProductListResult result = cartProductGateway.getProducts(productVariantIds);
        Map<Long, CartProductResult> resultMap = result.toMap();
        for (AddCartItemsCommand.Item item : command.items()) {
            if (resultMap.get(item.productVariantId()) == null) {
                throw new RuntimeException();
            }
            CartProductResult product = resultMap.get(item.productVariantId());
            if (product.status() != CartProductStatus.ON_SALE) {
                throw new RuntimeException();
            }
            if (product.stock() < item.quantity()) {
                throw new RuntimeException();
            }
        }
        List<CartItemData> cartItemData = cartCommandService.addCartItems(command);
        List<CartItemResult> returnResult = new ArrayList<>();
        for (CartItemData itemData : cartItemData) {
            CartProductResult product = resultMap.get(itemData.productVariantId());
            CartProductStatus status = product.status();
            CartItemAvailability availability;
            if (status == CartProductStatus.ON_SALE) {
                availability = CartItemAvailability.AVAILABLE;
            } else {
                availability = CartItemAvailability.NOT_FOR_SALE;
            }
            CartItemResult cartItemResult = CartItemResult.from(itemData, product, availability);
            returnResult.add(cartItemResult);
        }
        return CartResult.builder()
                .items(returnResult)
                .build();
    }

    public CartResult getCartDetails(Long userId) {
        return null;
    }

    public CartResult updateCartItemQuantity(CartCommand.UpdateQuantity command) {
        return null;
    }

    public void removeCartItems(Long userId, List<Long> cartItemIds) {
        cartService.deleteCartItems(userId, cartItemIds);
    }

    public void removePurchasedItems(Long userId, List<Long> productVariantIds) {
        cartService.deleteByProductVariantIds(userId, productVariantIds);
    }
}
