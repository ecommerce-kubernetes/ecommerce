package com.example.order_service.api.cart.application;

import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.api.cart.application.dto.command.AddCartItemDto;
import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
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
    private final CartService cartService;
    private final ProductClientService productClientService;

    public CartItemResponse addItem(AddCartItemDto dto) {
        ProductResponse product = productClientService.fetchProductByVariantId(dto.getProductVariantId());
        Long userId = dto.getUserPrincipal().getUserId();
        CartItemDto result = cartService.addItemToCart(userId, dto.getProductVariantId(), dto.getQuantity());
        return CartItemResponse.of(result, product);
    }

    public CartResponse getCartDetails(UserPrincipal userPrincipal){
        Long userId = userPrincipal.getUserId();
        List<CartItemDto> cartItems = cartService.getCartItems(userId);

        return Optional.of(cartItems)
                .filter(items -> !items.isEmpty())
                .map(this::fetchInfoAndMapToCartResponse)
                .orElseGet(CartResponse::ofEmpty);
    }

    public void removeCartItem(UserPrincipal userPrincipal, Long cartItemId){
        Long userId = userPrincipal.getUserId();
        cartService.deleteCartItem(userId, cartItemId);
    }

    public void clearCart(UserPrincipal userPrincipal){
        Long userId = userPrincipal.getUserId();
        cartService.clearCart(userId);
    }

    public CartItemResponse updateCartItemQuantity(UpdateQuantityDto dto){
        CartItemDto cartItem = cartService.getCartItem(dto.getCartItemId());
        ProductResponse product = productClientService.fetchProductByVariantId(cartItem.getProductVariantId());
        CartItemDto cartItemDto = cartService.updateQuantity(cartItem.getId(), dto.getQuantity());
        return CartItemResponse.of(cartItemDto, product);
    }

    private CartResponse fetchInfoAndMapToCartResponse(List<CartItemDto> cartItems){
        List<Long> productVariantIds = getProductVariantId(cartItems);
        List<ProductResponse> products = productClientService.fetchProductByVariantIds(productVariantIds);
        List<CartItemResponse> cartItemResponses = mapToCartItemResponse(cartItems, products);
        return CartResponse.from(cartItemResponses);
    }

    private List<Long> getProductVariantId(List<CartItemDto> cartItems){
        return cartItems.stream().map(CartItemDto::getProductVariantId).toList();
    }

    private List<CartItemResponse> mapToCartItemResponse(List<CartItemDto> cartItems, List<ProductResponse> products){
        Map<Long, ProductResponse> productMap = products.stream().collect(Collectors.toMap(
                ProductResponse::getProductVariantId,
                Function.identity(),
                (p1, p2) -> p1
        ));

        return cartItems.stream()
                .map(item -> createCartItemResponse(item, productMap.get(item.getProductVariantId())))
                .toList();
    }

    private CartItemResponse createCartItemResponse(CartItemDto item, ProductResponse product){
        if(product == null){
            return CartItemResponse.ofUnavailable(item.getId(), item.getQuantity());
        }
        return CartItemResponse.of(item.getId(), item.getQuantity(), product);
    }
}
