package com.example.order_service.controller;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.ErrorResponseEntityFactory;
import com.example.order_service.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.controller.util.validator.OrderPageableValidator;
import com.example.order_service.controller.util.validator.PageableValidatorFactory;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.*;
import com.example.order_service.entity.DomainType;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.SseConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.common.MessagePath.BAD_REQUEST_VALIDATION;
import static com.example.order_service.util.ControllerTestHelper.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({ErrorResponseEntityFactory.class})
class OrderControllerTest {

    private static final String BASE_PATH = "/orders";
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PendingOrderTimeoutScheduler pendingOrderTimeoutScheduler;
    @MockitoBean
    OrderService orderService;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    PageableValidatorFactory pageableValidatorFactory;
    @MockitoBean
    SseConnectionService sseConnectionService;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(NOT_FOUND)).thenReturn("NotFound");
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
    }

    @Test
    @DisplayName("주문 생성 테스트-성공")
    void createOrderTest_success() throws Exception {
        CreateOrderResponse expectedResponse = new CreateOrderResponse(1L, "http://order/1/subscribe");
        when(orderService.saveOrder(anyLong(), any(OrderRequest.class)))
                .thenReturn(expectedResponse);

        OrderRequest request = new OrderRequest(
                List.of(new OrderItemRequest(1L, 2)),
                "서울시 테헤란로 123", 1L, 4000L, 900L);
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc, post(BASE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), expectedResponse);
    }

    @Test
    @DisplayName("주문 생성 테스트-실패(헤더 없음)")
    void createOrderTest_noHeader() throws Exception {
        List<OrderItemRequest> orderItemRequests = List.of(new OrderItemRequest(1L, 10));
        OrderRequest request = new OrderRequest(orderItemRequests, "서울시 테헤란로 234", 1L, 3000L, 5000L);

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH);
    }

    @Test
    @DisplayName("주문 목록 조회 테스트-성공")
    void getOrdersTest_success() throws Exception {
        OrderResponse expectedResponse = new OrderResponse(1L, "COMPLETED", LocalDateTime.now(),
                List.of(new OrderItemResponse(1L, "상품1", "http://product1.jpg", 2,
                        new UnitPriceInfo(3000, 10, 300, 2700),
                        5400, List.of(new ItemOptionResponse("색상", "RED")))));
        PageDto<OrderResponse> orderResponsePageDto = new PageDto<>(List.of(expectedResponse), 1, 10, 10, 20);
        when(orderService.getOrderList(any(Pageable.class), anyLong(), any(), any()))
                .thenReturn(orderResponsePageDto);
        when(pageableValidatorFactory.getValidator(DomainType.ORDER)).thenReturn(new OrderPageableValidator());
        ResultActions perform = performWithBodyAndUserIdHeader(mockMvc,
                get(BASE_PATH).param("page", "1").param("size", "10"),
                null);

        verifySuccessResponse(perform, status().isOk(), orderResponsePageDto);
    }

    @Test
    @DisplayName("주문 목록 조회 테스트-실패(헤더 없음)")
    void getOrdersTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, get(BASE_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH);
    }
}