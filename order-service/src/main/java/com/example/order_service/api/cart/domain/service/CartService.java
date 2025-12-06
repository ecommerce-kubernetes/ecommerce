package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.service.dto.CartItemDto;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.api.cart.application.dto.result.CartItemResponse;
import com.example.order_service.api.cart.application.dto.result.CartResponse;
import com.example.order_service.api.cart.domain.model.CartItems;
import com.example.order_service.api.cart.domain.model.Carts;
import com.example.order_service.api.common.exception.NoPermissionException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.cart.domain.repository.CartItemsRepository;
import com.example.order_service.api.cart.domain.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.api.cart.application.dto.command.UpdateQuantityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService{
    private final ProductClientService productClientService;
    private final CartsRepository cartsRepository;
    private final CartItemsRepository cartItemsRepository;
    private final MessageSourceUtil ms;

    @Transactional
    public CartItemDto addItemToCart(Long userId, Long productVariantId, int quantity){
        Carts cart = cartsRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> cartsRepository.save(Carts.of(userId)));
        CartItems cartItem = cart.addItem(productVariantId, quantity);
        CartItems savedItem = cartItemsRepository.save(cartItem);
        return CartItemDto.of(savedItem);
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems(Long userId){
        return cartsRepository.findWithItemsByUserId(userId)
                .map(this::createCartItemDtoList)
                .orElseGet(List::of);
    }

    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId){
        CartItems cartItem = cartItemsRepository.findWithCartById(cartItemId)
                .orElseThrow(() -> new NotFoundException("장바구니에서 해당 상품을 찾을 수 없습니다"));

        Long cartUserId = cartItem.getCart().getUserId();
        if(!userId.equals(cartUserId)){
            throw new NoPermissionException("장바구니의 상품을 삭제할 권한이 없습니다");
        }
        cartItem.removeFromCart();
        cartItemsRepository.delete(cartItem);
    }

    @Transactional
    public void deleteCartItemById(UserPrincipal userPrincipal, Long cartItemId){
        CartItems cartItem = cartItemsRepository.findWithCartById(cartItemId)
                .orElseThrow(() -> new NotFoundException("장바구니에 해당 상품을 찾을 수 없습니다"));

        if(!cartItem.getCart().getUserId().equals(userPrincipal.getUserId())){
            throw new NoPermissionException("장바구니의 상품을 삭제할 권한이 없습니다");
        }
        cartItem.removeFromCart();
        cartItemsRepository.delete(cartItem);
    }

    @Transactional
    public void clearAllCartItems(UserPrincipal userPrincipal){
        Long userId = userPrincipal.getUserId();
        Optional<Carts> cart = cartsRepository.findWithItemsByUserId(userId);
        if(cart.isEmpty()){
            throw new NotFoundException("장바구니를 찾을 수 없습니다");
        }
        cart.get().clearItems();
    }

    public CartItemResponse updateCartItemQuantity(UpdateQuantityDto updateQuantityDto) {
        Long cartItemId = updateQuantityDto.getCartItemId();
        CartItems cartItem = cartItemsRepository.findWithCartById(cartItemId)
                .orElseThrow(() -> new NotFoundException("장바구니에 해당 상품을 찾을 수 없습니다"));

        if(!cartItem.getCart().getUserId().equals(updateQuantityDto.getUserPrincipal().getUserId())){
            throw new NoPermissionException("장바구니의 상품을 삭제할 권한이 없습니다");
        }

        try {
            ProductResponse product = productClientService.fetchProductByVariantId(cartItem.getProductVariantId());
            cartItem.addQuantity(updateQuantityDto.getQuantity());
            return CartItemResponse.of(cartItem.getId(), cartItem.getQuantity(), product);
        } catch (NotFoundException e) {
            return CartItemResponse.ofUnavailable(cartItem.getId(), cartItem.getQuantity());
        }
    }

    private List<CartItemResponse> mapToCartItemResponse(List<CartItems> cartItems, List<ProductResponse> products){
        Map<Long, ProductResponse> productMap = products.stream().collect(Collectors.toMap(
                ProductResponse::getProductVariantId,
                Function.identity(),
                (p1, p2) -> p1
        ));

        return cartItems.stream()
                .map(item -> createCartItemResponse(item, productMap.get(item.getProductVariantId())))
                .toList();
    }

    private CartItemResponse createCartItemResponse(CartItems item, ProductResponse product){
        if(product == null){
            return CartItemResponse.ofUnavailable(item.getId(), item.getQuantity());
        }
        return CartItemResponse.of(item.getId(), item.getQuantity(), product);
    }

    private List<Long> getProductVariantIds(List<CartItems> cartItems){
        return cartItems.stream()
                .map(CartItems::getProductVariantId).toList();
    }

    private List<CartItemDto> createCartItemDtoList(Carts cart){
        return cart.getCartItems().stream().map(CartItemDto::of)
                .toList();
    }
}
