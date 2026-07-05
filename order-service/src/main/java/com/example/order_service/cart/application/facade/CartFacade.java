package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.dto.result.CartItemResult;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.cart.application.service.CartService;
import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.CartCommand;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
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
    private final CartQueryService cartQueryService;
    private final CartProductGateway cartProductGateway;
    private final CartItemValidator cartItemValidator;

    public CartResult addItems(AddCartItemsCommand command) {
        List<Long> productVariantIds = command.toProductVariantIds();
        CartProductListResult result = cartProductGateway.getProducts(productVariantIds);
        Map<Long, CartProductResult> productMap = result.toMap();
        cartItemValidator.validate(command, productMap);
        cartCommandService.addCartItems(command);
        List<CartItemData> cartItemData = cartQueryService.getCartItems(command.userId());
        List<CartItemResult> returnResult = cartItemData.stream()
                .map(itemData -> {
                    CartProductResult product = productMap.get(itemData.productVariantId());
                    CartItemAvailability availability = determineAvailability(product.status());
                    return CartItemResult.from(itemData, product, availability);
                })
                .toList();
        return CartResult.builder()
                .items(returnResult)
                .build();
    }

    private CartItemAvailability determineAvailability(CartProductStatus status) {
        return status == CartProductStatus.ON_SALE
                ? CartItemAvailability.AVAILABLE
                : CartItemAvailability.NOT_FOR_SALE;
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
