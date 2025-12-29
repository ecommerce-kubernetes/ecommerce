package com.example.order_service.api.support;

import com.example.order_service.api.cart.application.CartApplicationService;
import com.example.order_service.api.cart.controller.CartController;
import com.example.order_service.api.notification.controller.NotificationController;
import com.example.order_service.api.notification.service.NotificationService;
import com.example.order_service.api.order.application.OrderApplicationService;
import com.example.order_service.api.order.controller.OrderController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CartController.class, OrderController.class, NotificationController.class})
public abstract class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected CartApplicationService cartApplicationService;
    @MockitoBean
    protected OrderApplicationService orderApplicationService;
    @MockitoBean
    protected NotificationService notificationService;
}
