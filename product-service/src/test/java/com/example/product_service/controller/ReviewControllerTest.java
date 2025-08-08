package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.product_service.controller.util.MessagePath.BAD_REQUEST;
import static com.example.product_service.controller.util.MessagePath.BAD_REQUEST_VALIDATION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    ReviewService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage("conflict")).thenReturn("Conflict");
    }
}
