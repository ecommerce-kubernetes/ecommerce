package com.example.order_service.service;

import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.dto.request.CartItemRequestDto;
import com.example.order_service.dto.response.CartItemResponseDto;
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
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto(1L, 10);

        ProductResponseDto productResponseDto = new ProductResponseDto(1L, "사과", "청송 사과 3EA", 3000, 50, 1L, "http://test.jpg");

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

    @Test
    @DisplayName("장바구니 아이템 조회")
    void getCartItemListTest(){
        Carts carts = new Carts(1L);
        CartItems cartItems = new CartItems(carts, 1L, 10);

        Carts savedCart = cartsRepository.save(carts);

        ProductResponseDto productResponseDto = new ProductResponseDto(1L, "사과", "청송 사과 3EA", 3000, 50, 1L,"http://test.jpg");
        when(productClientService.fetchProductBatch(any(ProductRequestIdsDto.class))).thenReturn(List.of(productResponseDto));
        CartResponseDto cartItemList = cartService.getCartItemList(1L);
        assertThat(cartItemList.getId()).isEqualTo(savedCart.getId());
        assertThat(cartItemList.getCartTotalPrice()).isEqualTo(productResponseDto.getPrice() * cartItems.getQuantity());

        List<CartItemResponseDto> returnCartItems = cartItemList.getCartItems();

        for (CartItemResponseDto returnCartItem : returnCartItems) {
            assertThat(returnCartItem.getProductId()).isEqualTo(productResponseDto.getId());
            assertThat(returnCartItem.getPrice()).isEqualTo(productResponseDto.getPrice());
            assertThat(returnCartItem.getQuantity()).isEqualTo(cartItems.getQuantity());
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