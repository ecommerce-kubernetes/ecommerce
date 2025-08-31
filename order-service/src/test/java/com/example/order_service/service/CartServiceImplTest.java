package com.example.order_service.service;

import com.example.order_service.dto.client.ProductImageDto;
import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.CompactProductResponseDto;
import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponseDto;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
class CartServiceImplTest {

    @Autowired
    CartService cartService;
    @Autowired
    CartsRepository cartsRepository;

    @MockitoBean
    ProductClientService productClientService;

    @AfterEach
    void initDB(){
        cartsRepository.deleteAll();
    }

    @Test
    @DisplayName("장바구니 아이템 추가")
    @Transactional
    void addItem(){
        CartItemRequest cartItemRequest = new CartItemRequest(1L, 10);

        ProductResponseDto productResponseDto = new ProductResponseDto(1L, "사과", "청송 사과 3EA", 3000, 50, 1L, List.of(new ProductImageDto(1L, "http://test.jpg",0)));

        when(productClientService.fetchProduct(1L)).thenReturn(productResponseDto);
        CartItemResponse cartItemResponse = cartService.addItem(1L, cartItemRequest);

        assertThat(cartItemResponse.getProductId()).isEqualTo(cartItemRequest.getProductVariantId());
        assertThat(cartItemResponse.getProductName()).isEqualTo(productResponseDto.getName());
        assertThat(cartItemResponse.getPrice()).isEqualTo(productResponseDto.getPrice());
        assertThat(cartItemResponse.getQuantity()).isEqualTo(cartItemRequest.getQuantity());
        //동일한 상품 추가
        CartItemResponse equalItemAddResponseDto = cartService.addItem(1L, cartItemRequest);
        assertThat(equalItemAddResponseDto.getProductId()).isEqualTo(cartItemRequest.getProductVariantId());
        assertThat(equalItemAddResponseDto.getProductName()).isEqualTo(productResponseDto.getName());
        assertThat(equalItemAddResponseDto.getPrice()).isEqualTo(productResponseDto.getPrice());
        assertThat(equalItemAddResponseDto.getQuantity()).isEqualTo(cartItemRequest.getQuantity() * 2);
        assertThat(equalItemAddResponseDto.getMainImgUrl()).isEqualTo(productResponseDto.getImages().get(0).getImageUrl());
    }

    @Test
    @DisplayName("장바구니 아이템 조회")
    void getCartItemListTest(){
        Carts carts = new Carts(1L);
        CartItems cartItems = new CartItems(carts, 1L, 10);

        Carts savedCart = cartsRepository.save(carts);

        CompactProductResponseDto compactProductResponseDto = new CompactProductResponseDto(1L, "사과", "청송 사과 3EA", 3000, 50, 1L,"http://test.jpg");
        when(productClientService.fetchProductBatch(any(ProductRequestIdsDto.class))).thenReturn(List.of(compactProductResponseDto));
        CartResponseDto cartItemList = cartService.getCartItemList(1L);
        assertThat(cartItemList.getId()).isEqualTo(savedCart.getId());
        assertThat(cartItemList.getCartTotalPrice()).isEqualTo(compactProductResponseDto.getPrice() * cartItems.getQuantity());

        List<CartItemResponse> returnCartItems = cartItemList.getCartItems();

        for (CartItemResponse returnCartItem : returnCartItems) {
            assertThat(returnCartItem.getProductId()).isEqualTo(compactProductResponseDto.getId());
            assertThat(returnCartItem.getPrice()).isEqualTo(compactProductResponseDto.getPrice());
            assertThat(returnCartItem.getQuantity()).isEqualTo(cartItems.getQuantity());
            assertThat(returnCartItem.getMainImgUrl()).isEqualTo(compactProductResponseDto.getMainImgUrl());
        }
    }

    @Test
    @DisplayName("장바구니 아이템 조회 - Not Found")
    void getCartItemListTest_NotFound(){
        assertThatThrownBy(() -> cartService.getCartItemList(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Not Found Cart");
    }

    @Test
    @DisplayName("장바구니 개별 삭제 테스트")
    @Transactional
    void deleteCartItemByIdTest(){
        Carts cart = new Carts(1L);
        CartItems cartItems1 = new CartItems(cart, 1L, 20);
        CartItems cartItems2 = new CartItems(cart, 2L, 30);

        cartsRepository.save(cart);

        Long id = cartItems1.getId();
        cartService.deleteCartItemById(id);

        Carts savedCart = cartsRepository.findById(cart.getId())
                .orElseThrow(() -> new NotFoundException("Not Found Cart"));

        assertThat(savedCart.getCartItems())
                .doesNotContain(cartItems1);
    }

    @Test
    @DisplayName("장바구니 일괄 삭제 테스트")
    @Transactional
    void deleteCartAllTest(){
        Carts cart = new Carts(1L);
        CartItems cartItems1 = new CartItems(cart, 1L, 20);
        CartItems cartItems2 = new CartItems(cart, 2L, 30);

        cartsRepository.save(cart);

        cartService.deleteCartAll(cart.getId());

        Carts savedCart = cartsRepository.findById(cart.getId())
                .orElseThrow(() -> new NotFoundException("Not Found Cart"));

        assertThat(savedCart.getCartItems().size()).isEqualTo(0);
    }
}