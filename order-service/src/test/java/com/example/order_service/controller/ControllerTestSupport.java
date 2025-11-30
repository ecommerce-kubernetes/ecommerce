package com.example.order_service.controller;

import com.example.order_service.controller.util.validator.PageableValidator;
import com.example.order_service.controller.util.validator.PageableValidatorFactory;
import com.example.order_service.service.CartService;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(controllers = {CartController.class, OrderController.class})
public abstract class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected CartService cartService;
    @MockitoBean
    protected OrderService orderService;
    @MockitoBean
    protected PageableValidatorFactory pageableValidatorFactory;

}
