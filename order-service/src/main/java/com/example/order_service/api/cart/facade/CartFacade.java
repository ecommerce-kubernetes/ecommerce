package com.example.order_service.api.cart.facade;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.cart.domain.service.CartProductService;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.facade.dto.command.CartCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemStatus;
import com.example.order_service.api.cart.facade.dto.result.CartResult;
import com.example.order_service.api.common.exception.business.BusinessException;
import com.example.order_service.api.common.exception.business.code.CartErrorCode;
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
    private final CartProductService cartProductService;

    public CartResult.Cart addItems(CartCommand.AddItems command) {
        List<Long> requestedIds = command.items().stream().map(CartCommand.Item::productVariantId).toList();
        List<CartProductInfo> productInfos = cartProductService.getProductInfos(requestedIds);
        //검증 로직
        if (productInfos.size() != requestedIds.size()) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_FOUND);
        }
        boolean hasUnorderableProduct = productInfos.stream()
                .anyMatch(info -> info.getStatus() != ProductStatus.ON_SALE);
        if (hasUnorderableProduct) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }
        List<CartItemDto> cartItems = cartService.addItemToCart(command);
        List<CartResult.CartItemResult> cartItemResults = mapToCartItemResult(cartItems, productInfos);
        return CartResult.Cart.from(cartItemResults);
    }

    public CartResult.Cart getCartDetails(Long userId){
        List<CartItemDto> cartItems = cartService.getCartItems(userId);
        //장바구니에 상품이 없는 경우 빈 장바구니 반환
        if(cartItems.isEmpty()) {
            return CartResult.Cart.empty();
        }
        List<Long> variantIds = getProductVariantId(cartItems);
        List<CartProductInfo> productInfos = cartProductService.getProductInfos(variantIds);
        List<CartResult.CartItemResult> cartItemResults = mapToCartItemResult(cartItems, productInfos);
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

    private List<Long> getProductVariantId(List<CartItemDto> cartItems){
        return cartItems.stream().map(CartItemDto::getProductVariantId).toList();
    }

    private List<CartResult.CartItemResult> mapToCartItemResult(List<CartItemDto> cartItems, List<CartProductInfo> products) {
        Map<Long, CartProductInfo> productMap = products.stream().collect(Collectors.toMap(
                CartProductInfo::getProductVariantId,
                Function.identity()
        ));

        return cartItems.stream()
                .map(item -> {
                    CartProductInfo product = productMap.get(item.getProductVariantId());
                    return createCartItemResult(item, product);
                }).toList();
    }

    private CartResult.CartItemResult createCartItemResult(CartItemDto item, CartProductInfo product) {
        if (product == null) {
            return CartResult.CartItemResult.unAvailable(item.getId(), item.getProductVariantId(), item.getQuantity());
        }
        return switch (product.getStatus()) {
            case ON_SALE -> CartResult.CartItemResult.of(item, product, CartItemStatus.AVAILABLE);
            case PREPARING -> CartResult.CartItemResult.of(item, product, CartItemStatus.PREPARING);
            case STOP_SALE -> CartResult.CartItemResult.of(item, product, CartItemStatus.STOP_SALE);
            case DELETED -> CartResult.CartItemResult.of(item, product, CartItemStatus.DELETED);
            case UNKNOWN -> CartResult.CartItemResult.unAvailable(item.getId(), product.getProductVariantId(), item.getQuantity());
        };
    }
}
