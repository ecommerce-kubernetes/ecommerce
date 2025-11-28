package com.example.order_service.service;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.dto.request.CartItemRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.order_service.common.MessagePath.CART_ITEM_NOT_FOUND;
import static com.example.order_service.common.MessagePath.CART_ITEM_NO_PERMISSION;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartService{
    private final ProductClientService productClientService;
    private final CartsRepository cartsRepository;
    private final CartItemsRepository cartItemsRepository;
    private final MessageSourceUtil ms;

    public CartItemResponse addItem(Long userId, CartItemRequest request) {
        ProductResponse productResponse = productClientService.fetchProductByVariantId(request.getProductVariantId());
        Carts cart = findCartOrCreate(userId);
        CartItems cartItem = cart.addItem(productResponse, request.getQuantity());
        cartItemsRepository.save(cartItem);
        return null;
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

    public void clearAllCartItems(Long userId) {
        Optional<Carts> cart = cartsRepository.findWithItemsByUserId(userId);
        cart.ifPresent(Carts::clearItems);
    }

    private CartItems findWithCartByIdOrThrow(Long cartItemId){
        return cartItemsRepository.findWithCartById(cartItemId)
                .orElseThrow(() -> new NotFoundException(ms.getMessage(CART_ITEM_NOT_FOUND)));
    }

    private Carts findCartOrCreate(Long userId){
        return cartsRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> cartsRepository.save(new Carts(userId)));
    }

}
