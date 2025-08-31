package com.example.order_service.controller;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.util.ControllerTestHelper.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {
    private static final String BASE_PATH = "/carts";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CartService cartService;
    @MockitoBean
    MessageSourceUtil ms;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(NOT_FOUND)).thenReturn("NotFound");
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(PRODUCT_VARIANT_NOT_FOUND)).thenReturn("No products found for that option");
    }

    @Test
    @DisplayName("장바구니 아이템 추가 테스트-성공")
    void addCartItemTest_success() throws Exception {
        CartItemResponse response = new CartItemResponse(1L, 1L, 1L, "productName", "http:http//test.jpg",
                List.of(new ItemOptionResponse("색상", "RED")), 3000, 10, 10);
        when(cartService.addItem(anyLong(), any(CartItemRequest.class))).thenReturn(response);

        CartItemRequest request = new CartItemRequest(1L, 10);

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("장바구니 아이템 추가 테스트-실패(주문 상품을 찾을 수 없음)")
    void addCartItemTest_notFound_productVariant_notFound() throws Exception {
        when(cartService.addItem(anyLong(), any(CartItemRequest.class)))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_VARIANT_NOT_FOUND)));
        CartItemRequest request = new CartItemRequest(1L, 10);
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND), getMessage(PRODUCT_VARIANT_NOT_FOUND),
                BASE_PATH);
    }

    @Test
    @DisplayName("장바구니 아이템 추가 테스트-실패(검증)")
    void addCartItemTest_validation() throws Exception {
        CartItemRequest request = new CartItemRequest();
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST), getMessage(BAD_REQUEST_VALIDATION),
                BASE_PATH);

        perform
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @DisplayName("장바구니 아이템 추가 테스트-실패(헤더 없음)")
    void addCartItemTest_noHeader() throws Exception {
        CartItemRequest request = new CartItemRequest(1L, 10);
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);

        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH);
    }
}