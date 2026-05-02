package com.example.order_service.cart.application;

import com.example.order_service.cart.application.dto.command.CartCommand;
import com.example.order_service.cart.application.dto.result.CartProductResult;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.cart.application.dto.result.ProductStatus;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.domain.service.CartService;
import com.example.order_service.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.CartErrorCode;
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
public class CartAppService {
    private final CartService cartService;
    private final CartProductGateway cartProductGateway;

    public CartResult.Cart addItems(CartCommand.AddItems command) {
        List<Long> requestedIds = command.toProductVariantIds();
        List<CartProductResult.Info> products = cartProductGateway.getProducts(requestedIds);

        //상품 검증
        validateProductForAddCart(products, requestedIds);

        List<CartItemDto> cartItems = cartService.addItemToCart(command);
        List<CartResult.CartItemResult> cartItemResults = mapToCartItemResult(cartItems, products);
        return CartResult.Cart.from(cartItemResults);
    }

    public CartResult.Cart getCartDetails(Long userId){
        List<CartItemDto> cartItems = cartService.getCartItems(userId);
        //장바구니에 상품이 없는 경우 빈 장바구니 반환
        if(cartItems.isEmpty()) {
            return CartResult.Cart.empty();
        }
        List<Long> variantIds = getProductVariantId(cartItems);
        List<CartProductResult.Info> products = cartProductGateway.getProducts(variantIds);
        List<CartResult.CartItemResult> cartItemResults = mapToCartItemResult(cartItems, products);
        return CartResult.Cart.from(cartItemResults);
    }

    public CartResult.Update updateCartItemQuantity(CartCommand.UpdateQuantity command){
        CartItemDto cartItemDto = cartService.updateQuantity(command.userId(), command.cartItemId(), command.quantity());
        return CartResult.Update.from(cartItemDto);
    }

    public void removeCartItems(Long userId, List<Long> cartItemIds){
        cartService.deleteCartItems(userId, cartItemIds);
    }

    public void removePurchasedItems(Long userId, List<Long> productVariantIds) {
        cartService.deleteByProductVariantIds(userId, productVariantIds);
    }

    private void validateProductForAddCart(List<CartProductResult.Info> products, List<Long> variantIds) {
        // 누락된 상품이 있는지 검증
        if (products.size() != variantIds.size()){
            throw new BusinessException(CartErrorCode.CART_PRODUCT_NOT_FOUND);
        }

        // 추가할 상품이 추가 가능한지 검증
        for(CartProductResult.Info product: products) {
            if (product.status() != ProductStatus.AVAILABLE) {
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
