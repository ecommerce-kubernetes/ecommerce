package com.example.order_service.controller.order;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.advice.ErrorResponseEntityFactory;
import com.example.order_service.common.scheduler.PendingOrderTimeoutScheduler;
import com.example.order_service.config.TestConfig;
import com.example.order_service.controller.OrderController;
import com.example.order_service.controller.util.validator.PageableValidatorFactory;
import com.example.order_service.service.OrderService;
import com.example.order_service.service.SseConnectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.common.MessagePath.BAD_REQUEST_VALIDATION;
import static com.example.order_service.util.ControllerTestHelper.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
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
    @DisplayName("주문 목록 조회 테스트-실패(헤더 없음)")
    void getOrdersTest_noHeader() throws Exception {
        ResultActions perform = performWithBody(mockMvc, get(BASE_PATH), null);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                "Required request header 'X-User-Id' for method parameter type Long is not present",
                BASE_PATH);
    }
}