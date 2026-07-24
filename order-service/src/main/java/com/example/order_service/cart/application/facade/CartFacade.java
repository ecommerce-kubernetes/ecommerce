package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.*;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartFacade {
    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;
    private final CartProductGateway cartProductGateway;
    private final CartItemValidator cartItemValidator;

    public AddCartItemsResult addItems(AddCartItemsCommand command) {
        List<Long> variantIds = command.toProductVariantIds();
        CartProductResult productData = cartProductGateway.getProducts(variantIds);

        cartItemValidator.validate(command, productData);

        cartCommandService.addCartItems(command);

        List<CartItemData> cartItems = cartQueryService.findCartItemsByVariantIds(command.userId(), variantIds);
        return AddCartItemsResult.from(cartItems);
    }

    public CartResult getCartDetails(Long userId) {
        List<CartItemData> cartItems = cartQueryService.findCartItems(userId);

        if (cartItems.isEmpty()) {
            return CartResult.empty();
        }

        List<Long> variantIds = cartItems.stream().map(CartItemData::productVariantId).toList();
        CartProductResult productData = cartProductGateway.getProducts(variantIds);

        return createCartResult(productData, cartItems);
    }

    public CartItemResult getCartItemDetails(Long userId, Long cartItemId) {
        CartItemData cartItem = cartQueryService.getCartItem(userId, cartItemId);

        CartProductResult productData = cartProductGateway.getProducts(List.of(cartItem.productVariantId()));

        Map<Long, CartProductResult.CartProductDetail> productMap = productData.toMap();
        CartProductResult.CartProductDetail product = productMap.get(cartItem.productVariantId());

        return createCartItemResult(product, cartItem);
    }

    private CartResult createCartResult(CartProductResult productData, List<CartItemData> cartItems) {
        Map<Long, CartProductResult.CartProductDetail> productMap = productData.toMap();
        List<CartItemResult> list = cartItems.stream()
                .map(item -> createCartItemResult(productMap.get(item.productVariantId()), item))
                .toList();
        return CartResult.builder()
                .items(list)
                .build();
    }

    private CartItemResult createCartItemResult(CartProductResult.CartProductDetail product, CartItemData cartItem) {
        if (product == null) {
            return CartItemResult.unknown(cartItem, CartItemAvailability.NOT_FOR_SALE);
        }
        CartItemAvailability availability = determineAvailability(product.status(), product, cartItem.quantity());
        return CartItemResult.from(cartItem, product, availability);
    }

    private CartItemAvailability determineAvailability(CartProductStatus status, CartProductResult.CartProductDetail product, int cartQuantity) {
        if (status != CartProductStatus.ON_SALE) {
            return CartItemAvailability.NOT_FOR_SALE;
        }

        if (product.stock() < cartQuantity) {
            return CartItemAvailability.LACK_OF_STOCK;
        }

        return CartItemAvailability.AVAILABLE;
    }

    public UpdateCartItemQuantityResult updateCartItemQuantity(UpdateCartItemQuantityCommand command) {
        cartCommandService.updateCartItemQuantity(command);

        CartItemData cartItem = cartQueryService.getCartItem(command.userId(), command.cartItemId());

        return UpdateCartItemQuantityResult.from(cartItem);
    }

    public void deleteCartItems(DeleteCartItemsCommand command) {
        cartCommandService.deleteCartItems(command);
    }
}
