package com.example.order_service.controller;

import com.example.order_service.common.advice.ControllerAdvice;
import com.example.order_service.dto.request.CartItemRequestDto;
import com.example.order_service.dto.response.CartItemResponseDto;
import com.example.order_service.dto.response.CartResponseDto;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(ControllerAdvice.class)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CartService cartService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("장바구니 추가")
    void addCartItemTest() throws Exception {
        Long userId = 1L;

        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto( 1L, 10);
        CartItemResponseDto cartItemResponseDto = new CartItemResponseDto(1L, 1L, "사과", 3000, 10);
        when(cartService.addItem(anyLong(), any(CartItemRequestDto.class))).thenReturn(cartItemResponseDto);

        String content = mapper.writeValueAsString(cartItemRequestDto);

        ResultActions perform = mockMvc.perform(post("/carts")
                .header("user-id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        perform
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(cartItemResponseDto.getId()))
                .andExpect(jsonPath("$.productId").value(cartItemResponseDto.getProductId()))
                .andExpect(jsonPath("$.productName").value(cartItemResponseDto.getProductName()))
                .andExpect(jsonPath("$.price").value(cartItemResponseDto.getPrice()))
                .andExpect(jsonPath("$.quantity").value(cartItemResponseDto.getQuantity()));
    }

    @Test
    @DisplayName("장바구니 추가 - 없는 상품 추가시")
    void addCartItemTest_NotFoundProduct() throws Exception {
        Long userId = 1L;
        CartItemRequestDto cartItemRequestDto = new CartItemRequestDto( 1L, 10);
        doThrow(new NotFoundException("Not Found Product")).when(cartService).addItem(anyLong(),any(CartItemRequestDto.class));

        String content = mapper.writeValueAsString(cartItemRequestDto);

        ResultActions perform = mockMvc.perform(post("/carts")
                .header("user-id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Product"))
                .andExpect(jsonPath("$.path").value("/carts"));
    }

    @Test
    @DisplayName("장바구니 조회")
    void getAllCartItemTest() throws Exception {
        List<CartItemResponseDto> cartItems = List.of(new CartItemResponseDto(1L, 1L, "사과", 3000, 10),
                new CartItemResponseDto(2L, 2L, "바나나", 5000, 5));

        int cartTotalPrice = 0;
        for (CartItemResponseDto cartItem : cartItems) {
            cartTotalPrice =+ cartItem.getPrice() * cartItem.getQuantity();
        }
        CartResponseDto responseDto = new CartResponseDto(1L, cartItems, cartTotalPrice);

        when(cartService.getCartItemList(anyLong())).thenReturn(responseDto);

        ResultActions perform = mockMvc.perform(get("/carts/1"));

        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cartTotalPrice").value(responseDto.getCartTotalPrice()));

        for(int i=0; i<responseDto.getCartItems().size(); i++){
            List<CartItemResponseDto> cartItemResponseDtoList = responseDto.getCartItems();
            perform
                    .andExpect(jsonPath("$.cartItems[" + i + "].id").value(cartItemResponseDtoList.get(i).getId()))
                    .andExpect(jsonPath("$.cartItems[" + i + "].productId").value(cartItemResponseDtoList.get(i).getProductId()))
                    .andExpect(jsonPath("$.cartItems[" + i + "].productName").value(cartItemResponseDtoList.get(i).getProductName()))
                    .andExpect(jsonPath("$.cartItems[" + i + "].price").value(cartItemResponseDtoList.get(i).getPrice()))
                    .andExpect(jsonPath("$.cartItems[" + i + "].quantity").value(cartItemResponseDtoList.get(i).getQuantity()));
        }
    }

    @Test
    @DisplayName("장바구니 아이템 개별 삭제 테스트")
    void removeCartItemTest() throws Exception {
        doNothing().when(cartService).deleteCartItemById(anyLong());

        ResultActions perform = mockMvc.perform(delete("/carts/1"));

        perform
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("장바구니 아이템 개별 삭제 테스트 _ 장바구니 아이탬을 찾을 수 없을때")
    void removeCartItemTest_NotFound() throws Exception {
        doThrow(new NotFoundException("Not Found CartItem")).when(cartService).deleteCartItemById(anyLong());

        ResultActions perform = mockMvc.perform(delete("/carts/1"));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found CartItem"))
                .andExpect(jsonPath("$.path").value("/carts/1"));
    }

    @Test
    @DisplayName("장바구니 일괄 삭제")
    void removeCartAllTest() throws Exception {
        doNothing().when(cartService).deleteCartAll(anyLong());

        ResultActions perform = mockMvc.perform(delete("/carts/1/all"));

        perform
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("장바구니 일괄 삭제 _ 장바구니 찾을수 없을때")
    void removeCartAllTest_NotFound() throws Exception {
        doThrow(new NotFoundException("Not Found Cart")).when(cartService).deleteCartAll(anyLong());

        ResultActions perform = mockMvc.perform(delete("/carts/1/all"));

        perform
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NotFound"))
                .andExpect(jsonPath("$.message").value("Not Found Cart"))
                .andExpect(jsonPath("$.path").value("/carts/1/all"));
    }
}