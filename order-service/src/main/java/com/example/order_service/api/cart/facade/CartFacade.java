package com.example.order_service.api.cart.facade;

import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartFacade {
    private final CartService cartService;
    private final CartProductClientService cartProductClientService;

    public CartItemResponse addItem(AddCartItemCommand dto) {
        CartProductResponse product = cartProductClientService.getProduct(dto.getProductVariantId());
        validateProductOnSale(product);
        CartItemDto result = cartService.addItemToCart(dto.getUserId(), dto.getProductVariantId(), dto.getQuantity());
        return CartItemResponse.available(result, product);
    }

    public CartResponse getCartDetails(Long userId){
        List<CartItemDto> cartItems = cartService.getCartItems(userId);
        //장바구니에 상품이 없는 경우 빈 장바구니 반환
        if(cartItems.isEmpty()) {
            return CartResponse.empty();
        }
        List<Long> variantIds = getProductVariantId(cartItems);
        List<CartProductResponse> products = cartProductClientService.getProducts(variantIds);

        List<CartItemResponse> cartItemResponses = mapToCartItemResponse(cartItems, products);
        return CartResponse.from(cartItemResponses);
    }

    public CartItemResponse updateCartItemQuantity(UpdateQuantityCommand dto){
        CartItemDto cartItem = cartService.getCartItem(dto.getUserId(), dto.getCartItemId());
        CartProductResponse product = cartProductClientService.getProduct(cartItem.getProductVariantId());
        if (product == null || !product.isOnSale()) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }
        CartItemDto cartItemDto = cartService.updateQuantity(dto.getUserId(), cartItem.getId(), dto.getQuantity());
        return CartItemResponse.available(cartItemDto, product);
    }

    public void removeCartItem(Long userId, Long cartItemId){
        cartService.deleteCartItem(userId, cartItemId);
    }

    public void clearCart(Long userId){
        cartService.clearCart(userId);
    }

    public void cleanUpCartAfterOrder(Long userId, List<Long> productVariantIds) {
        cartService.deleteByProductVariantIds(userId, productVariantIds);
    }

    private List<Long> getProductVariantId(List<CartItemDto> cartItems){
        return cartItems.stream().map(CartItemDto::getProductVariantId).toList();
    }

    private List<CartItemResponse> mapToCartItemResponse(List<CartItemDto> cartItems, List<CartProductResponse> products){
        Map<Long, CartProductResponse> productMap = products.stream().collect(Collectors.toMap(
                CartProductResponse::getProductVariantId,
                Function.identity()
        ));

        return cartItems.stream()
                .map(item -> {
                    CartProductResponse product = productMap.get(item.getProductVariantId());
                    return createCartItemResponse(item, product);
                }).toList();
    }

    private CartItemResponse createCartItemResponse(CartItemDto item, CartProductResponse product){
        // 상품을 찾을 수 없거나 상품이 판매중이 아닌 상품인 경우 오류 응답
        if(product == null){
            return CartItemResponse.unAvailable(item.getId(), item.getProductVariantId(), item.getQuantity());
        }
        return switch (product.getStatus()) {
            case ON_SALE -> CartItemResponse.available(item, product);
            case PREPARING -> CartItemResponse.preparing(item, product);
            case STOP_SALE -> CartItemResponse.stop_sale(item, product);
            case DELETED -> CartItemResponse.deleted(item, product);
        };
    }

    private void validateProductOnSale(CartProductResponse product) {
        if (product == null || !product.isOnSale()) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }
    }
}
