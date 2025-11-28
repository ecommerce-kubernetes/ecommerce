package com.example.order_service.controller.cart;

import com.example.order_service.controller.ControllerTestSupport;
import com.example.order_service.controller.dto.CartItemRequest;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.util.ControllerTestHelper.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerTest extends ControllerTestSupport {
    private static final String BASE_PATH = "/carts";
    private static final String ID_PATH = BASE_PATH + "/1";

    @Test
    @DisplayName("")
    void addCartItem() {
        //given
        CartItemRequest request = CartItemRequest.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        //when
        //then
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