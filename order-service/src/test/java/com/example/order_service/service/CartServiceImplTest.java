package com.example.order_service.service;

import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.dto.request.CartItemRequestDto;
import com.example.order_service.dto.response.CartItemResponseDto;
import com.example.order_service.entity.Carts;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.ProductClientService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    @DisplayName("장바구니 아이템 추가")
    void addItem(){
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto(1L, 10);

        ProductResponseDto productResponseDto = new ProductResponseDto(1L, "사과", "청송 사과 3EA", 3000, 50, 1L);

        when(productClientService.fetchProduct(1L)).thenReturn(productResponseDto);
        CartItemResponseDto cartItemResponseDto = cartService.addItem(1L, cartItemRequestDto);

        assertThat(cartItemResponseDto.getProductId()).isEqualTo(cartItemRequestDto.getProductId());
        assertThat(cartItemResponseDto.getProductName()).isEqualTo(productResponseDto.getName());
        assertThat(cartItemResponseDto.getPrice()).isEqualTo(productResponseDto.getPrice());
        assertThat(cartItemResponseDto.getQuantity()).isEqualTo(cartItemRequestDto.getQuantity());
        //동일한 상품 추가
        CartItemResponseDto equalItemAddResponseDto = cartService.addItem(1L, cartItemRequestDto);
        assertThat(equalItemAddResponseDto.getProductId()).isEqualTo(cartItemRequestDto.getProductId());
        assertThat(equalItemAddResponseDto.getProductName()).isEqualTo(productResponseDto.getName());
        assertThat(equalItemAddResponseDto.getPrice()).isEqualTo(productResponseDto.getPrice());
        assertThat(equalItemAddResponseDto.getQuantity()).isEqualTo(cartItemRequestDto.getQuantity() * 2);
    }
}