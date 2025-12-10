package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartDomainService;
import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartApplicationService {
    private final CartDomainService cartDomainService;
    private final CartProductClientService cartProductClientService;

    public CartItemResponse addItem(AddCartItemDto dto) {
        CartProductResponse product = cartProductClientService.getProduct(dto.getProductVariantId());
        Long userId = dto.getUserPrincipal().getUserId();
        CartItemDto result = cartDomainService.addItemToCart(userId, dto.getProductVariantId(), dto.getQuantity());
        return CartItemResponse.of(result, product);
    }

    public CartResponse getCartDetails(UserPrincipal userPrincipal){
        Long userId = userPrincipal.getUserId();
        List<CartItemDto> cartItems = cartDomainService.getCartItems(userId);

        return Optional.of(cartItems)
                .filter(items -> !items.isEmpty())
                .map(this::fetchInfoAndMapToCartResponse)
                .orElseGet(CartResponse::ofEmpty);
    }

    public void removeCartItem(UserPrincipal userPrincipal, Long cartItemId){
        Long userId = userPrincipal.getUserId();
        cartDomainService.deleteCartItem(userId, cartItemId);
    }

    public void clearCart(UserPrincipal userPrincipal){
        Long userId = userPrincipal.getUserId();
        cartDomainService.clearCart(userId);
    }

    public CartItemResponse updateCartItemQuantity(UpdateQuantityDto dto){
        CartItemDto cartItem = cartDomainService.getCartItem(dto.getCartItemId());
        try {
            CartProductResponse product = cartProductClientService.getProduct(cartItem.getProductVariantId());
            CartItemDto cartItemDto = cartDomainService.updateQuantity(cartItem.getId(), dto.getQuantity());
            return CartItemResponse.of(cartItemDto, product);
        } catch (NotFoundException e){
            return CartItemResponse.ofUnavailable(cartItem.getId(), cartItem.getQuantity());
        }
    }

    private CartResponse fetchInfoAndMapToCartResponse(List<CartItemDto> cartItems){
        List<Long> productVariantIds = getProductVariantId(cartItems);
        List<CartProductResponse> products = cartProductClientService.getProducts(productVariantIds);
        List<CartItemResponse> cartItemResponses = mapToCartItemResponse(cartItems, products);
        return CartResponse.from(cartItemResponses);
    }

    private List<Long> getProductVariantId(List<CartItemDto> cartItems){
        return cartItems.stream().map(CartItemDto::getProductVariantId).toList();
    }

    private List<CartItemResponse> mapToCartItemResponse(List<CartItemDto> cartItems, List<CartProductResponse> products){
        Map<Long, CartProductResponse> productMap = products.stream().collect(Collectors.toMap(
                CartProductResponse::getProductVariantId,
                Function.identity(),
                (p1, p2) -> p1
        ));

        return cartItems.stream()
                .map(item -> createCartItemResponse(item, productMap.get(item.getProductVariantId())))
                .toList();
    }

    private CartItemResponse createCartItemResponse(CartItemDto item, CartProductResponse product){
        if(product == null){
            return CartItemResponse.ofUnavailable(item.getId(), item.getQuantity());
        }
        return CartItemResponse.of(item, product);
    }
}
