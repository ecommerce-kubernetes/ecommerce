package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartDomainService;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartApplicationService {
    private final CartDomainService cartDomainService;
    private final CartProductClientService cartProductClientService;

    public CartItemResponse addItem(AddCartItemDto dto) {
        CartProductResponse product = cartProductClientService.getProduct(dto.getProductVariantId());
        CartItemDto result = cartDomainService.addItemToCart(dto.getUserId(), dto.getProductVariantId(), dto.getQuantity());
        return CartItemResponse.available(result, product);
    }

    public CartResponse getCartDetails(Long userId){
        List<CartItemDto> cartItems = cartDomainService.getCartItems(userId);
        //장바구니에 상품이 없는 경우 빈 장바구니 반환
        if(cartItems.isEmpty()) {
            return CartResponse.empty();
        }
        List<Long> variantIds = getProductVariantId(cartItems);
        List<CartProductResponse> products = cartProductClientService.getProducts(variantIds);

        List<CartItemResponse> cartItemResponses = mapToCartItemResponse(cartItems, products);
        return CartResponse.from(cartItemResponses);
    }

    public CartItemResponse updateCartItemQuantity(UpdateQuantityDto dto){
        CartItemDto cartItem = cartDomainService.getCartItem(dto.getUserId(), dto.getCartItemId());
        CartProductResponse product = cartProductClientService.getProduct(cartItem.getProductVariantId());
        CartItemDto cartItemDto = cartDomainService.updateQuantity(dto.getUserId(), cartItem.getId(), dto.getQuantity());
        return CartItemResponse.available(cartItemDto, product);
    }

    public void removeCartItem(Long userId, Long cartItemId){
        cartDomainService.deleteCartItem(userId, cartItemId);
    }

    public void clearCart(Long userId){
        cartDomainService.clearCart(userId);
    }

    public void cleanUpCartAfterOrder(Long userId, List<Long> productVariantIds) {
        cartDomainService.deleteByProductVariantIds(userId, productVariantIds);
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
        //상품서비스에서 응답이 오지 않은 상품이라면 오류 응답
        if(product == null){
            return CartItemResponse.unAvailable(item.getId(), item.getProductVariantId(), item.getQuantity());
        }
        //정상적으로 응답이 온 경우 정상 응답
        return CartItemResponse.available(item, product);
    }
}
