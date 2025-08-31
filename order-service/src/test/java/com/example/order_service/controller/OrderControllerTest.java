package com.example.order_service.controller;

import com.example.order_service.common.advice.ControllerAdvice;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(OrderController.class)
@Import(ControllerAdvice.class)
class OrderControllerTest {

}