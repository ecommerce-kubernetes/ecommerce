package com.example.order_service.service;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.security.UserPrincipal;
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

    // 단순 조회 로직인 메서드에서 외부 서비스 호출 로직이 메서드 안에 존재하므로
    // 서비스 레이어에서 @Transactional을 사용하지 않고 리포지토리 레이어의 @Transactional 사용
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
}
