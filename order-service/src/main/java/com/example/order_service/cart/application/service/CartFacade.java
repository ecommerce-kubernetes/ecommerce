package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.external.dto.result.CartProductStatus;
import com.example.order_service.cart.application.service.dto.command.CartCommand;
import com.example.order_service.cart.application.external.dto.result.CartProductResult;
import com.example.order_service.cart.application.service.dto.result.CartResult;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.service.dto.result.CartItemDto;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartFacade {
    private final CartService cartService;
    private final CartProductGateway cartProductGateway;

    public CartResult.Cart addItems(CartCommand.AddItems command) {
        List<Long> requestedIds = command.toProductVariantIds();
        CartProductResult.ProductList productResult = cartProductGateway.getProducts(requestedIds);
        Map<Long, Integer> quantityMap = command.toQuantityMap();
        for(CartProductResult.Info product : productResult.products()) {
            if (!quantityMap.containsKey(product.productVariantId())) {
                throw new BusinessException(CartErrorCode.CART_PRODUCT_NOT_FOUND);
            }
            if (product.status() != CartProductStatus.ON_SALE) {
                throw new BusinessException(CartErrorCode.CART_PRODUCT_CANNOT_ADD);
            }
            Integer quantity = quantityMap.get(product.productVariantId());
            if (quantity > product.stock()) {
                throw new BusinessException(CartErrorCode.CART_PRODUCT_STOCK_INSUFFICIENT);
            }
        }
        List<CartItemDto> cartItems = cartService.addItemToCart(command);
        return null;
    }

    public CartResult.Cart getCartDetails(Long userId){
//        List<CartItemDto> cartItems = cartService.getCartItems(userId);
//        if(cartItems.isEmpty()) {
//            return CartResult.Cart.empty();
//        }
//        List<Long> variantIds = getProductVariantId(cartItems);
//        List<CartProductResult.Info> products = cartProductGateway.getProducts(variantIds);
//        List<CartResult.CartItemResult> cartItemResults = mapToCartItemResult(cartItems, products);
//        return CartResult.Cart.from(cartItemResults);
        return null;
    }

    public CartResult.Cart updateCartItemQuantity(CartCommand.UpdateQuantity command){
        CartItemDto cartItemDto = cartService.updateQuantity(command.userId(), command.cartItemId(), command.quantity());
//        return CartResult.Update.from(cartItemDto);
        return null;
    }

    public void removeCartItems(Long userId, List<Long> cartItemIds){
        cartService.deleteCartItems(userId, cartItemIds);
    }

    public void removePurchasedItems(Long userId, List<Long> productVariantIds) {
        cartService.deleteByProductVariantIds(userId, productVariantIds);
    }

    private void validateProductForAddCart(List<CartProductResult.Info> products, List<Long> variantIds) {
        if (products.size() != variantIds.size()){
            throw new BusinessException(CartErrorCode.CART_PRODUCT_NOT_FOUND);
        }
        for(CartProductResult.Info product: products) {
            if (product.status() != CartProductStatus.ON_SALE) {
                throw new BusinessException(CartErrorCode.CART_PRODUCT_CANNOT_ADD);
            }
        }
    }

    private List<Long> getProductVariantId(List<CartItemDto> cartItems){
        return cartItems.stream().map(CartItemDto::getProductVariantId).toList();
    }

    private List<CartResult.CartItemResult> mapToCartItemResult(List<CartItemDto> cartItems, List<CartProductResult.Info> products) {
        Map<Long, CartProductResult.Info> productMap = products.stream().collect(Collectors.toMap(
                CartProductResult.Info::productVariantId,
                Function.identity()
        ));

        return cartItems.stream()
                .map(item -> {
                    CartProductResult.Info product = productMap.get(item.getProductVariantId());
                    return createCartItemResult(item, product);
                }).toList();
    }

    private CartResult.CartItemResult createCartItemResult(CartItemDto item, CartProductResult.Info product) {
        if (product == null) {
            return CartResult.CartItemResult.unAvailable(item.getId(), item.getProductVariantId(), item.getQuantity());
        }
        return CartResult.CartItemResult.of(item, product);
    }
}
