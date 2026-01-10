package com.example.product_service.api.support;

import com.example.product_service.api.category.controller.CategoryController;
import com.example.product_service.common.advice.ErrorResponseEntityFactory;
import com.example.product_service.api.category.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CategoryController.class, DummyController.class})
public abstract class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected CategoryService categoryService;
    //TODO 삭제
    @MockitoBean
    protected ErrorResponseEntityFactory factory;
}
