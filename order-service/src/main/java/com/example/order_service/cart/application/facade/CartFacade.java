package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.DeleteCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.AddCartItemsResult;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.dto.result.CartItemResult;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.dto.result.CartResult;
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
        CartProductListResult productData = cartProductGateway.getProducts(variantIds);
        cartItemValidator.validate(command, productData);
        cartCommandService.addCartItems(command);
        List<CartItemData> cartItems = cartQueryService.getCartItems(command.userId(), variantIds);
        return AddCartItemsResult.from(cartItems);
    }

    public CartResult getCartDetails(Long userId) {
        List<CartItemData> cartItems = cartQueryService.getCartItems(userId);
        return assembleResult(cartItems);
    }

    public CartResult updateCartItemQuantity(UpdateCartItemQuantityCommand command) {
        cartCommandService.updateCartItemQuantity(command);
        List<CartItemData> cartItems = cartQueryService.getCartItems(command.userId());
        return assembleResult(cartItems);
    }

    public void removeCartItems(DeleteCartItemsCommand command) {
        cartCommandService.deleteCartItems(command.userId(), command.cartItemIds());
    }

    private CartResult assembleResult(List<CartItemData> cartItemData) {
        List<Long> variantIds = cartItemData.stream().map(CartItemData::productVariantId).toList();
        CartProductListResult result = cartProductGateway.getProducts(variantIds);
        Map<Long, CartProductResult> productMap = result.toMap();
        List<CartItemResult> returnResult = cartItemData.stream()
                .map(item -> {
                    CartProductResult product = productMap.get(item.productVariantId());
                    if (product == null) {
                        return CartItemResult.unknown(item, CartItemAvailability.NOT_FOR_SALE);
                    }
                    CartItemAvailability availability = determineAvailability(product.status(), product, item.quantity());
                    return CartItemResult.from(item, product, availability);
                })
                .toList();
        return CartResult.builder()
                .items(returnResult)
                .build();
    }
    private CartItemAvailability determineAvailability(CartProductStatus status, CartProductResult product, int cartQuantity) {
        if (status != CartProductStatus.ON_SALE) {
            return CartItemAvailability.NOT_FOR_SALE;
        }

        if (product.stock() < cartQuantity) {
            return CartItemAvailability.LACK_OF_STOCK;
        }

        return CartItemAvailability.AVAILABLE;
    }
}
