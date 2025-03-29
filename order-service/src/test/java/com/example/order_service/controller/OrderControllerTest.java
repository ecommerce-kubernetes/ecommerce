package com.example.order_service.controller;

import com.example.order_service.service.OrderService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @MockitoBean
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;


}