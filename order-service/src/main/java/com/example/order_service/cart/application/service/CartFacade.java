package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.port.CartProductPort;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.service.dto.data.CartItemData;
import com.example.order_service.cart.application.service.dto.result.*;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
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
    private final CartProductPort cartProductPort;
    private final CartQueryService cartQueryService;
    private final CartItemValidator cartItemValidator;
    private final CartContextFactory contextFactory;

    public AddCartItemsResult addItems(AddCartItemsCommand command) {
        List<Long> variantIds = command.toProductVariantIds();
        CartProductResult productData = cartProductPort.getProducts(variantIds);
        Map<Long, CartProductResult.CartProductDetail> productDataMap = productData.toMap();

        command.items().forEach(item ->
                cartItemValidator.validateAddable(productDataMap.get(item.productVariantId())));

        AddCartItemsContext context = contextFactory.toAddCartItemsContext(command, productData);
        List<Long> cartItems = cartCommandService.addCartItems(command.userId(), context);

        return AddCartItemsResult.from(cartItems);
    }

    public CartResult getCartDetails(Long userId) {
        List<CartItemData> cartItems = cartQueryService.findCartItems(userId);

        if (cartItems.isEmpty()) {
            return CartResult.empty();
        }

        List<Long> variantIds = cartItems.stream().map(CartItemData::productVariantId).toList();
        CartProductResult productData = cartProductPort.getProducts(variantIds);

        return createCartResult(productData, cartItems);
    }

    public CartItemResult getCartItemDetails(Long userId, Long cartItemId) {
        CartItemData cartItem = cartQueryService.getCartItem(userId, cartItemId);

        CartProductResult productData = cartProductPort.getProducts(List.of(cartItem.productVariantId()));

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
        CartItemData cartItem = cartQueryService.getCartItem(command.userId(), command.cartItemId());

        CartProductResult products = cartProductPort.getProducts(List.of(cartItem.productVariantId()));
        Map<Long, CartProductResult.CartProductDetail> productsMap = products.toMap();

        CartProductResult.CartProductDetail product = productsMap.get(cartItem.productVariantId());
        cartItemValidator.validateAddable(product);

        UpdateCartItemContext context = contextFactory.toUpdateContext(command, cartItem, product);
        cartCommandService.updateCartItemQuantity(command.userId(), context);

        return UpdateCartItemQuantityResult.from(cartItem);
    }

    public void deleteCartItems(DeleteCartItemsCommand command) {
        cartCommandService.deleteCartItems(command.userId(), command.cartItemIds());
    }
}
