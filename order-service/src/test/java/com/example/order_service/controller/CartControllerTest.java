package com.example.order_service.controller;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.ErrorResponseEntityFactory;
import com.example.order_service.config.TestConfig;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.ProductInfo;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
class CartControllerTest {
    private static final String BASE_PATH = "/carts";
    private static final String ID_PATH = BASE_PATH + "/1";

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
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(FORBIDDEN)).thenReturn("Forbidden");
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
    @DisplayName("장바구니 목록 조회 테스트-실패(헤더 없음)")
    void getAllCartItemTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, get(BASE_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-성공")
    void removeCartItemTest_success() throws Exception {
        doNothing().when(cartService).deleteCartItemById(anyLong(), anyLong());
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(헤더 없음)")
    void removeCartItemTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                ID_PATH
        );
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 찾을 수 없음)")
    void removeCartItemTest_notFound_cartItem() throws Exception {
        doThrow(new NotFoundException(getMessage(CART_ITEM_NOT_FOUND)))
                .when(cartService).deleteCartItemById(anyLong(), anyLong());
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CART_ITEM_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(삭제할 권한이 없음)")
    void removeCartItemTest_noPermission() throws Exception {
        doThrow(new NoPermissionException(getMessage(CART_ITEM_NO_PERMISSION)))
                .when(cartService).deleteCartItemById(anyLong(), anyLong());

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isForbidden(), getMessage(FORBIDDEN),
                getMessage(CART_ITEM_NO_PERMISSION), ID_PATH);
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-성공")
    void clearCartTest_success() throws Exception {
        doNothing().when(cartService).clearAllCartItems(anyLong());
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-실패(헤더 없음)")
    void clearCartTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, delete(BASE_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH
        );
    }
}