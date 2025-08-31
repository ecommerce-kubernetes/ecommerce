package com.example.order_service.service;

import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.CompactProductResponseDto;
import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartItemsRepository;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CartServiceImpl implements CartService{
    private final ProductClientService productClientService;
    private final CartsRepository cartsRepository;
    private final CartItemsRepository cartItemsRepository;

    @Transactional
    @Override
    public CartItemResponse addItem(Long userId, CartItemRequest cartItemRequest) {

        Carts cart = cartsRepository.findByUserId(userId).orElse(null);

        CartItems cartItem;

        if(cart == null){
            cart = new Carts(userId);
            cartItem = new CartItems(cart, cartItemRequest.getProductVariantId(), cartItemRequest.getQuantity());
        }
        else {
            cartItem = cart.getCartItems().stream()
                    .filter(item -> Objects.equals(item.getProductId(), cartItemRequest.getProductVariantId()))
                    .findFirst()
                    .orElse(null);

            if(cartItem != null){
                cartItem.addQuantity(cartItemRequest.getQuantity());
            }
            else {
                cartItem = new CartItems(cart, cartItemRequest.getProductVariantId(), cartItemRequest.getQuantity());
            }
        }

        Carts savedCart = cartsRepository.save(cart);
        CartItems persistedCartItem = savedCart.getCartItems().stream()
                .filter(item -> Objects.equals(item.getProductId(), cartItemRequest.getProductVariantId()))
                .findFirst()
                .orElse(cartItem);

        ProductResponseDto productResponseDto = productClientService.fetchProduct(cartItem.getProductId());
        return null;
    }

    @Override
    public CartResponse getCartItemList(Long userId) {
        Carts cart = cartsRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Not Found Cart"));

        List<CartItems> cartItems = cart.getCartItems();

        List<Long> ids = cartItems.stream().map(
                CartItems::getProductId
        ).toList();
        List<CompactProductResponseDto> products =
                productClientService.fetchProductBatch(new ProductRequestIdsDto(ids));
        Map<Long, CompactProductResponseDto> productMap = products.stream()
                .collect(Collectors.toMap(CompactProductResponseDto::getId, Function.identity()));

        List<AbstractMap.SimpleEntry<CompactProductResponseDto, CartItems>> cartItemsList =
                cartItems.stream()
                        .map(item -> new AbstractMap.SimpleEntry<>(
                                productMap.get(item.getProductId()),
                                item
                        )).toList();

        int cartTotalPrice = cartItemsList.stream()
                .mapToInt(entry -> entry.getKey().getPrice() * entry.getValue().getQuantity())
                .sum();

        return null;
    }

    @Transactional
    @Override
    public void deleteCartItemById(Long userId, Long cartItemId) {
        CartItems cartItem = cartItemsRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Not Found CartItem"));

        Carts cart = cartItem.getCart();
        cart.removeCartItem(cartItem);
    }

    @Transactional
    @Override
    public void deleteCartAll(Long cartId) {
        Carts cart = cartsRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Not Found Cart"));

        cart.getCartItems().clear();
    }

    @Transactional
    @Override
    public void deleteCartItemByProductId(Long productId) {
        List<CartItems> cartItems = cartItemsRepository.findByProductId(productId);
        for (CartItems cartItem : cartItems) {
            Carts cart = cartItem.getCart();
            cart.removeCartItem(cartItem);
        }
    }
}
