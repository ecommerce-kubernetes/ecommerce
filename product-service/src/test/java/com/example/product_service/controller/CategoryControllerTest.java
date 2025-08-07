package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.ControllerTestHelper;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    private static final String ICON_URL = "http://test.jpg";
    private static final String BASE_PATH = "/categories";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    CategoryService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage("badRequest")).thenReturn("BadRequest");
        when(ms.getMessage("badRequest.validation")).thenReturn("Validation Error");
        when(ms.getMessage("conflict")).thenReturn("Conflict");
    }

    @Test
    @DisplayName("카테고리 생성 테스트-성공")
    void createCategoryTest_success() throws Exception {
        CategoryRequest request = new CategoryRequest("category", 1L, ICON_URL);
        CategoryResponse response = new CategoryResponse(1L, "category", 1L, ICON_URL);
        when(service.saveCategory(any(CategoryRequest.class)))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("카테고리 생성 테스트-실패(검증)")
    void createCategoryTest_validation() throws Exception {
        CategoryRequest request = new CategoryRequest("", null, "invalid");
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(),
                getMessage("badRequest"), getMessage("badRequest.validation"), BASE_PATH);

        perform
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("카테고리 생성 테스트-실패(부모 카테고리 없음)")
    void createCategoryTest_notFound() throws Exception {
        CategoryRequest request = new CategoryRequest("childCategory", 1L, ICON_URL);
        when(service.saveCategory(any(CategoryRequest.class)))
                .thenThrow(new NotFoundException(getMessage("category.notFound")));
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("category.notFound"), BASE_PATH);

    }

    @Test
    @DisplayName("카테고리 생성 테스트-실패(중복)")
    void createCategoryTest_conflict() throws Exception {
        CategoryRequest request = new CategoryRequest("duplicated", 1L, ICON_URL);

        when(service.saveCategory(any(CategoryRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage("category.conflict")));
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage("conflict"),
                getMessage("category.conflict"), BASE_PATH);
    }

}