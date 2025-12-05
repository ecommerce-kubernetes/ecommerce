package com.example.order_service.support;

import com.example.order_service.api.cart.application.CartApplicationService;
import com.example.order_service.api.cart.controller.CartController;
import com.example.order_service.api.order.controller.OrderController;
import com.example.order_service.controller.util.validator.PageableValidatorFactory;
import com.example.order_service.api.cart.domain.service.CartService;
import com.example.order_service.service.order.OrderService;
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
    //TODO 계층 분리로 제거
    @MockitoBean
    protected CartService cartService;
    @MockitoBean
    protected OrderService orderService;
    @MockitoBean
    protected PageableValidatorFactory pageableValidatorFactory;
    @MockitoBean
    protected CartApplicationService cartApplicationService;

}
