package com.example.order_service.controller;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.ErrorResponseEntityFactory;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.*;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.common.MessagePath.BAD_REQUEST_VALIDATION;
import static com.example.order_service.util.ControllerTestHelper.performWithBodyAndUserIdHeader;
import static com.example.order_service.util.ControllerTestHelper.verifyErrorResponse;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
        OrderResponse expectedResponse = new OrderResponse(
                1L, "PENDING", LocalDateTime.now(), "서울시 테헤란로 123",
                new OrderSummary(6000, 1100, 4900),
                new PaymentDetails(4000, 900),
                new CouponDetails(1L, 500),
                new OrderItemSummary(
                        List.of(new OrderItemResponse(1L, "상품1", 2, 3000, 10,
                                List.of(new ItemOptionResponse("색상", "RED")),
                                "http://product1.jpg")), 5400)
        );
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenReturn(expectedResponse);

        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)),
                "서울시 테헤란로 123", 1L, 4000, 900);
        OrderResponse response = orderService.saveOrder(1L, request);


        assertThat(response)
                .extracting(OrderResponse::getId, OrderResponse::getStatus, OrderResponse::getDeliveryAddress)
                .containsExactlyInAnyOrder(1L, "PENDING", "서울시 테헤란로 123");

        assertThat(response.getOrderSummary())
                .extracting(OrderSummary::getProductTotal, OrderSummary::getDiscount, OrderSummary::getFinalPayment)
                .containsExactlyInAnyOrder(6000, 1100, 4900);

        assertThat(response.getPaymentDetails())
                .extracting(PaymentDetails::getUsedCash, PaymentDetails::getUsedReserve)
                .containsExactlyInAnyOrder(4000, 900);

        assertThat(response.getCouponDetails())
                .extracting(CouponDetails::getCouponId, CouponDetails::getDiscountAmount)
                .containsExactlyInAnyOrder(1L, 500);

        assertThat(response.getOrderItemSummary().getItemFinalPrice()).isEqualTo(5400);
        assertThat(response.getOrderItemSummary().getItems())
                .extracting(OrderItemResponse::getProductId, OrderItemResponse::getProductName, OrderItemResponse::getPrice,
                        OrderItemResponse::getQuantity, OrderItemResponse::getDiscountRate, OrderItemResponse::getThumbNailUrl)
                .containsExactlyInAnyOrder(
                        tuple(1L, "상품1", 3000, 2, 10, "http://product1.jpg")
                );

        assertThat(response.getOrderItemSummary().getItems())
                .flatExtracting(OrderItemResponse::getOptions)
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(
                    tuple("색상", "RED")
                );
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