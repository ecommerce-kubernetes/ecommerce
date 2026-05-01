package com.example.order_service.cart.application;

import com.example.order_service.cart.application.dto.command.CartCommand;
import com.example.order_service.cart.application.dto.result.CartItemStatus;
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
        List<Long> requestedIds = command.items().stream().map(CartCommand.Item::productVariantId).toList();
        List<CartProductResult.Info> products = cartProductGateway.getProducts(requestedIds);

        // 장바구니 추가 요청한 상품 종류와 조회된 상품 종류의 개수가 서로 다른경우 검증
        if (products.size() != requestedIds.size()) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_FOUND);
        }
        //판매중이 아닌 상품인 경우 검증
        boolean hasUnorderableProduct = products.stream()
                .anyMatch(product -> product.status() != ProductStatus.ON_SALE);
        if (hasUnorderableProduct) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }
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
        return switch (product.status()) {
            case ON_SALE -> CartResult.CartItemResult.of(item, product, CartItemStatus.AVAILABLE);
            case PREPARING -> CartResult.CartItemResult.of(item, product, CartItemStatus.PREPARING);
            case STOP_SALE -> CartResult.CartItemResult.of(item, product, CartItemStatus.STOP_SALE);
            case DELETED -> CartResult.CartItemResult.of(item, product, CartItemStatus.DELETED);
            case UNKNOWN -> CartResult.CartItemResult.unAvailable(item.getId(), product.productVariantId(), item.getQuantity());
        };
    }
}
