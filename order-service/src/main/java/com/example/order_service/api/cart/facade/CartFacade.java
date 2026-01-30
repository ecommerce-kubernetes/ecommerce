package com.example.order_service.api.cart.facade;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.cart.domain.service.CartProductService;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.domain.service.dto.result.CartItemDto;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.facade.dto.command.AddCartItemCommand;
import com.example.order_service.api.cart.facade.dto.command.UpdateQuantityCommand;
import com.example.order_service.api.cart.facade.dto.result.CartItemResponse;
import com.example.order_service.api.cart.facade.dto.result.CartResponse;
import com.example.order_service.api.cart.infrastructure.client.CartProductAdaptor;
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
    private final CartProductService cartProductService;

    public CartItemResponse addItem(AddCartItemCommand dto) {
        CartProductInfo productInfo = cartProductService.getProductInfo(dto.getProductVariantId());
        CartItemDto result = cartService.addItemToCart(dto.getUserId(), productInfo.getProductVariantId(), dto.getQuantity());
        return CartItemResponse.available(result, productInfo);
    }

    public CartResponse getCartDetails(Long userId){
        List<CartItemDto> cartItems = cartService.getCartItems(userId);
        //장바구니에 상품이 없는 경우 빈 장바구니 반환
        if(cartItems.isEmpty()) {
            return CartResponse.empty();
        }
        List<Long> variantIds = getProductVariantId(cartItems);
        List<CartProductInfo> productInfos = cartProductService.getProductInfos(variantIds);
        List<CartItemResponse> cartItemResponses = mapToCartItemResponse(cartItems, productInfos);
        return CartResponse.from(cartItemResponses);
    }

    public CartItemResponse updateCartItemQuantity(UpdateQuantityCommand dto){
        CartItemDto cartItem = cartService.getCartItem(dto.getUserId(), dto.getCartItemId());
        CartProductInfo productInfo = cartProductService.getProductInfo(cartItem.getProductVariantId());
        CartItemDto cartItemDto = cartService.updateQuantity(dto.getUserId(), cartItem.getId(), dto.getQuantity());
        return CartItemResponse.available(cartItemDto, productInfo);
    }

    public void removeCartItem(Long userId, Long cartItemId){
        cartService.deleteCartItem(userId, cartItemId);
    }

    public void clearCart(Long userId){
        cartService.clearCart(userId);
    }

    public void removePurchasedItems(Long userId, List<Long> productVariantIds) {
        cartService.deleteByProductVariantIds(userId, productVariantIds);
    }

    private List<Long> getProductVariantId(List<CartItemDto> cartItems){
        return cartItems.stream().map(CartItemDto::getProductVariantId).toList();
    }

    private List<CartItemResponse> mapToCartItemResponse(List<CartItemDto> cartItems, List<CartProductInfo> products){
        Map<Long, CartProductInfo> productMap = products.stream().collect(Collectors.toMap(
                CartProductInfo::getProductVariantId,
                Function.identity()
        ));

        return cartItems.stream()
                .map(item -> {
                    CartProductInfo product = productMap.get(item.getProductVariantId());
                    return createCartItemResponse(item, product);
                }).toList();
    }

    private CartItemResponse createCartItemResponse(CartItemDto item, CartProductInfo product){
        // 상품을 찾을 수 없거나 상품이 판매중이 아닌 상품인 경우 오류 응답
        if(product == null){
            return CartItemResponse.unAvailable(item.getId(), item.getProductVariantId(), item.getQuantity());
        }
        return switch (product.getStatus()) {
            case ON_SALE -> CartItemResponse.available(item, product);
            case PREPARING -> CartItemResponse.preparing(item, product);
            case STOP_SALE -> CartItemResponse.stop_sale(item, product);
            case DELETED -> CartItemResponse.deleted(item, product);
            case UNKNOWN -> CartItemResponse.unAvailable(item.getId(), product.getProductVariantId(), item.getQuantity());
        };
    }
}
