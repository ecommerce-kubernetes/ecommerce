package com.example.order_service.api.support;

import com.example.order_service.api.cart.application.CartApplicationService;
import com.example.order_service.api.cart.controller.CartController;
import com.example.order_service.api.order.controller.OrderController;
import com.example.order_service.api.common.util.validator.PageableValidatorFactory;
import com.example.order_service.api.order.domain.service.OrderService;
import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.service.SseConnectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CartController.class, OrderController.class})
public abstract class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected OrderService orderService;
    @MockitoBean
    protected PageableValidatorFactory pageableValidatorFactory;
    @MockitoBean
    protected CartApplicationService cartApplicationService;
    @MockitoBean
    protected MessageSourceUtil ms;
    @MockitoBean
    protected SseConnectionService sseConnectionService;

}
