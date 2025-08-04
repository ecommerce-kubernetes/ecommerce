package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.CategoryController;
import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.request.ModifyCategoryRequest;
import com.example.product_service.service.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
class CategoryControllerSecurityTest {

    private static final String BASE_PATH = "/categories";
    private static final String CATEGORY_ID_PATH = "/categories/1";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CategoryService categoryService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("카테고리 생성 테스트-인증 에러")
    void createCategoryTest_UnAuthorized() throws Exception {
        String jsonBody = createCategoryRequestJsonBody();
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody));


        verifyUnauthorizedResponse(perform, BASE_PATH);
    }
    @Test
    @DisplayName("카테고리 생성 테스트-권한 부족")
    void createCategoryTest_NoPermission() throws Exception {
        String jsonBody = createCategoryRequestJsonBody();
        ResultActions perform = mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", 1)
                        .header("X-User-Role", "ROLE_USER")
                        .content(jsonBody));

        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-인증 에러")
    void updateCategoryTest_UnAuthorized() throws Exception {
        String jsonBody = createModifyCategoryRequestJsonBody();
        ResultActions perform = mockMvc.perform(
                patch(CATEGORY_ID_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody));

        verifyUnauthorizedResponse(perform, CATEGORY_ID_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-권한 부족")
    void updateCategoryTest_NoPermission() throws Exception {
        String jsonBody = createModifyCategoryRequestJsonBody();

        ResultActions perform = mockMvc.perform(
                patch(CATEGORY_ID_PATH)
                        .header("X-User-Id", 1)
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody));

        verifyNoPermissionResponse(perform, CATEGORY_ID_PATH);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-인증 에러")
    void deleteCategoryTest_UnAuthorized() throws Exception {
        ResultActions perform = mockMvc.perform(
                delete(CATEGORY_ID_PATH)
                        .contentType(MediaType.APPLICATION_JSON));

        verifyUnauthorizedResponse(perform, CATEGORY_ID_PATH);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-권한 부족")
    void deleteCategoryTest_NoPermission() throws Exception {
        ResultActions perform = mockMvc.perform(
                delete(CATEGORY_ID_PATH)
                        .header("X-User-Id", 1)
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON));

        verifyNoPermissionResponse(perform, CATEGORY_ID_PATH);
    }

    private void verifyNoPermissionResponse(ResultActions perform, String path) throws Exception {
        verityErrorResponse(perform, status().isForbidden(), "Forbidden", "Access Denied", path);
    }
    private void verifyUnauthorizedResponse(ResultActions perform, String path) throws Exception {
        verityErrorResponse(perform, status().isUnauthorized(), "UnAuthorized", "Invalid Header", path);
    }

    private void verityErrorResponse(ResultActions perform, ResultMatcher status, String error, String message, String path) throws Exception {
        perform
                .andExpect(status)
                .andExpect(jsonPath("$.error").value(error))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(path));
    }

    private String createModifyCategoryRequestJsonBody() throws JsonProcessingException {
        ModifyCategoryRequest request = new ModifyCategoryRequest("name", 1L, "http://test.jpg");
        return createJsonBody(request);
    }

    private String createCategoryRequestJsonBody() throws JsonProcessingException {
        CategoryRequest request = new CategoryRequest("name", 1L, "http://test.jpg");
        return createJsonBody(request);
    }

    private String createJsonBody(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

}