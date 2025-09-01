package com.example.order_service.controller;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.ErrorResponseEntityFactory;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.OrderItemResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.exception.BadRequestException;
import com.example.order_service.exception.InsufficientException;
import com.example.order_service.exception.InvalidResourceException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.service.OrderService;
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
import static com.example.order_service.common.MessagePath.BAD_REQUEST_VALIDATION;
import static com.example.order_service.util.ControllerTestHelper.performWithBodyAndUserIdHeader;
import static com.example.order_service.util.ControllerTestHelper.verifyErrorResponse;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(ErrorResponseEntityFactory.class)
class OrderControllerTest {

    private static final String BASE_PATH = "/orders";
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;
    @MockitoBean
    MessageSourceUtil ms;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(NOT_FOUND)).thenReturn("NotFound");
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
    }

    @Test
    @DisplayName("주문 생성 테스트-성공")
    void createOrderTest_success(){

    }

    @Test
    @DisplayName("주문 생성 테스트-실패(주문 상품을 찾을 수 없음)")
    void createOrderTest_notFound_orderItem() throws Exception {
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_VARIANT_NOT_FOUND)));

        List<OrderItemRequest> orderItemRequests = List.of(new OrderItemRequest(1L, 10));
        OrderRequest request = new OrderRequest(orderItemRequests, "서울시 테헤란로 234", 1L, 3000, 5000);

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND), getMessage(PRODUCT_VARIANT_NOT_FOUND), BASE_PATH);
    }

    @Test
    @DisplayName("주문 생성 테스트-실패(주문 상품 수량 부족)")
    void createOrderTest_OutOfStock() throws Exception {
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenThrow(new InsufficientException(getMessage(OUT_OF_STOCK)));

        List<OrderItemRequest> orderItemRequests = List.of(new OrderItemRequest(1L, 10));
        OrderRequest request = new OrderRequest(orderItemRequests, "서울시 테헤란로 234", 1L, 3000, 5000);

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT), getMessage(OUT_OF_STOCK), BASE_PATH);
    }

    @Test
    @DisplayName("주문 생성 테스트-실패(캐시 부족)")
    void createOrderTest_insufficient_cash() throws Exception {
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenThrow(new InsufficientException(getMessage(INSUFFICIENT_CASH)));
        List<OrderItemRequest> orderItemRequests = List.of(new OrderItemRequest(1L, 10));
        OrderRequest request = new OrderRequest(orderItemRequests, "서울시 테헤란로 234", 1L, 3000, 5000);

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT), getMessage(INSUFFICIENT_CASH), BASE_PATH);
    }

    @Test
    @DisplayName("주문 생성 테스트-실패(적립금 부족)")
    void createOrderTest_insufficient_reserve() throws Exception {
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenThrow(new InsufficientException(getMessage(INSUFFICIENT_RESERVE)));

        List<OrderItemRequest> orderItemRequests = List.of(new OrderItemRequest(1L, 10));
        OrderRequest request = new OrderRequest(orderItemRequests, "서울시 테헤란로 234", 1L, 3000, 5000);

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT), getMessage(INSUFFICIENT_RESERVE), BASE_PATH);
    }

    @Test
    @DisplayName("주문 생성 테스트-실패(유효하지 않은 쿠폰)")
    void createOrderTest_invalid_coupon() throws Exception {
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenThrow(new InvalidResourceException(getMessage(INVALID_COUPON)));

        List<OrderItemRequest> orderItemRequests = List.of(new OrderItemRequest(1L, 10));
        OrderRequest request = new OrderRequest(orderItemRequests, "서울시 테헤란로 234", 1L, 3000, 5000);

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT), getMessage(INVALID_COUPON), BASE_PATH);
    }

    @Test
    @DisplayName("주문 생성 테스트-실패(주문금액과 최종 금액이 맞지 않음)")
    void createOrderTest_not_match_order_amount() throws Exception {
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenThrow(new BadRequestException(getMessage(NOT_MATCH_ORDER_AMOUNT)));

        List<OrderItemRequest> orderItemRequests = List.of(new OrderItemRequest(1L, 10));
        OrderRequest request = new OrderRequest(orderItemRequests, "서울시 테헤란로 234", 1L, 3000, 5000);

        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST), getMessage(NOT_MATCH_ORDER_AMOUNT), BASE_PATH);
    }
}