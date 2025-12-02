package com.example.order_service.service;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.common.security.UserRole;
import com.example.order_service.controller.dto.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartItemsRepository;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.service.dto.AddCartItemDto;
import com.example.order_service.service.dto.UpdateQuantityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.order_service.common.MessagePath.CART_ITEM_NOT_FOUND;
import static com.example.order_service.common.MessagePath.CART_ITEM_NO_PERMISSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService{
    private final ProductClientService productClientService;
    private final CartsRepository cartsRepository;
    private final CartItemsRepository cartItemsRepository;
    private final MessageSourceUtil ms;

    @Transactional
    public CartItemResponse addItem(AddCartItemDto dto){
        UserPrincipal userPrincipal = dto.getUserPrincipal();
        Long userId = userPrincipal.getUserId();

        Carts cart = cartsRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> cartsRepository.save(
                        Carts.of(userId)
                ));

        ProductResponse product = productClientService.fetchProductByVariantId(dto.getProductVariantId());
        CartItems cartItem = cart.addItem(dto.getProductVariantId(), dto.getQuantity());
        CartItems save = cartItemsRepository.save(cartItem);

        return CartItemResponse.of(save.getId(), save.getQuantity(), product);
    }

    public CartResponse getCartItemList(UserPrincipal userPrincipal){
        Long userId = userPrincipal.getUserId();
        Optional<Carts> cart = cartsRepository.findWithItemsByUserId(userId);
        if(cart.isEmpty()){
            return CartResponse.ofEmpty();
        } else {
            List<CartItems> cartItems = cart.get().getCartItems();
            List<Long> productVariantIds = getProductVariantIds(cartItems);
            List<ProductResponse> products = productClientService.fetchProductByVariantIds(productVariantIds);
            List<CartItemResponse> cartItemResponses = mapToCartItemResponse(cartItems, products);
            return CartResponse.from(cartItemResponses);
        }
    }

    @Transactional(readOnly = true)
    public CartResponse getCartItemList(Long userId) {
        Optional<Carts> optionalCart = cartsRepository.findWithItemsByUserId(userId);
        if(optionalCart.isEmpty()){
            return CartResponse.builder()
                    .cartItems(List.of())
                    .cartTotalPrice(0)
                    .build();
        }

        Carts cart = optionalCart.get();

        if(cart.getCartItems().isEmpty()){
            return CartResponse.builder()
                    .cartItems(List.of())
                    .cartTotalPrice(0)
                    .build();
        }
        return null;
    }

    private Map<Long, ProductResponse> fetchProductResponseToMap(List<CartItems> items){
        List<Long> ids = items.stream().map(CartItems::getProductVariantId).toList();
        return productClientService.fetchProductByVariantIds(ids)
                .stream().collect(Collectors.toMap(ProductResponse::getProductVariantId, Function.identity()));
    }

    public void deleteCartItemById(Long userId, Long cartItemId) {
        CartItems cartItem = findWithCartByIdOrThrow(cartItemId);
        if(!cartItem.getCart().getUserId().equals(userId)){
            throw new NoPermissionException(ms.getMessage(CART_ITEM_NO_PERMISSION));
        }
        cartItem.getCart().removeCartItem(cartItem);
    }

    public void deleteCartItemById(UserPrincipal userPrincipal, Long cartItemId){

    }

    public void clearAllCartItems(UserPrincipal userPrincipal){

    }

    public CartItemResponse updateCartItemQuantity(UpdateQuantityDto updateQuantityDto) {
        return null;
    }

    public void clearAllCartItems(Long userId) {
        Optional<Carts> cart = cartsRepository.findWithItemsByUserId(userId);
        cart.ifPresent(Carts::clearItems);
    }

    private CartItems findWithCartByIdOrThrow(Long cartItemId){
        return cartItemsRepository.findWithCartById(cartItemId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CART_ITEM_NOT_FOUND)));
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
}
