package com.example.order_service.service;

import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.dto.request.CartItemRequestDto;
import com.example.order_service.dto.request.OrderItemRequestDto;
import com.example.order_service.dto.response.CartItemResponseDto;
import com.example.order_service.dto.response.CartResponseDto;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NotFoundException;
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

    @Transactional
    @Override
    public CartItemResponseDto addItem(Long userId, CartItemRequestDto cartItemRequestDto) {

        Carts cart = cartsRepository.findByUserId(userId).orElse(null);

        CartItems cartItem;

        if(cart == null){
            cart = new Carts(userId);
            cartItem = new CartItems(cart, cartItemRequestDto.getProductId(), cartItemRequestDto.getQuantity());
        }
        else {
            cartItem = cart.getCartItems().stream()
                    .filter(item -> Objects.equals(item.getProductId(), cartItemRequestDto.getProductId()))
                    .findFirst()
                    .orElse(null);

            if(cartItem != null){
                cartItem.addQuantity(cartItemRequestDto.getQuantity());
            }
            else {
                cartItem = new CartItems(cart, cartItemRequestDto.getProductId(), cartItemRequestDto.getQuantity());
            }
        }

        Carts savedCart = cartsRepository.save(cart);
        CartItems persistedCartItem = savedCart.getCartItems().stream()
                .filter(item -> Objects.equals(item.getProductId(), cartItemRequestDto.getProductId()))
                .findFirst()
                .orElse(cartItem);

        ProductResponseDto productResponseDto = productClientService.fetchProduct(cartItem.getProductId());
        return new CartItemResponseDto(
                persistedCartItem.getId(),
                persistedCartItem.getProductId(),
                productResponseDto.getName(),
                productResponseDto.getPrice(),
                persistedCartItem.getQuantity()
        );
    }

    @Override
    public CartResponseDto getCartItemList(Long userId) {
        Carts cart = cartsRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Not Found Cart"));

        List<CartItems> cartItems = cart.getCartItems();

        List<Long> ids = cartItems.stream().map(
                CartItems::getProductId
        ).toList();
        List<ProductResponseDto> products =
                productClientService.fetchProductBatch(new ProductRequestIdsDto(ids));
        Map<Long, ProductResponseDto> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponseDto::getId, Function.identity()));

        List<AbstractMap.SimpleEntry<ProductResponseDto, CartItems>> cartItemsList =
                cartItems.stream()
                        .map(item -> new AbstractMap.SimpleEntry<>(
                                productMap.get(item.getProductId()),
                                item
                        )).toList();

        int cartTotalPrice = cartItemsList.stream()
                .mapToInt(entry -> entry.getKey().getPrice() * entry.getValue().getQuantity())
                .sum();

        List<CartItemResponseDto> cartItemResponseDtoList = cartItemsList.stream().map(entry ->
                new CartItemResponseDto(entry.getValue().getId(),
                        entry.getValue().getProductId(),
                        entry.getKey().getName(),
                        entry.getKey().getPrice(),
                        entry.getValue().getQuantity())).toList();

        return new CartResponseDto(cart.getId(), cartItemResponseDtoList, cartTotalPrice);
    }
}
