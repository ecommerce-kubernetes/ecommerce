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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(CategoryController.class)
@Import({WebSecurity.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
@AutoConfigureMockMvc
class CategoryControllerSecurityTest {

    private static String BASE_PATH = "/categories";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    CategoryService categoryService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("카테고리 생성 테스트-인증 에러")
    void createCategoryTest_UnAuthorized() throws Exception {
        CategoryRequest request = new CategoryRequest("name", 1L, "http://test.jpg");
        String jsonBody = mapper.writeValueAsString(request);
        ResultActions perform =
                mockMvc.perform(
                        post(BASE_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody));

        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UnAuthorized"))
                .andExpect(jsonPath("$.message").value("Invalid Header"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    @Test
    @DisplayName("카테고리 생성 테스트-권한 부족")
    void createCategoryTest_NoPermission() throws Exception {
        CategoryRequest request = new CategoryRequest("name", 1L, "http://test.jpg");
        String jsonBody = mapper.writeValueAsString(request);
        ResultActions perform = mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", 1)
                        .header("X-User-Role", "ROLE_USER")
                        .content(jsonBody));

        perform
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(BASE_PATH));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-인증 에러")
    void updateCategoryTest_UnAuthorized() throws Exception {
        ModifyCategoryRequest request = new ModifyCategoryRequest("name", 1L, "http://test.jpg");
        String jsonBody = mapper.writeValueAsString(request);

        ResultActions perform = mockMvc.perform(
                patch(BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody));

        perform
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UnAuthorized"))
                .andExpect(jsonPath("$.message").value("Invalid Header"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(BASE_PATH + "/1"));
    }

    @Test
    @DisplayName("카테고리 수정 테스트-권한 부족")
    void updateCategoryTest_NoPermission() throws Exception {
        ModifyCategoryRequest request = new ModifyCategoryRequest("name", 1L, "http://test.jpg");
        String jsonBody = mapper.writeValueAsString(request);

        ResultActions perform = mockMvc.perform(
                patch(BASE_PATH + "/1")
                        .header("X-User-Id", 1)
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody));

        perform
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value(BASE_PATH + "/1"));
    }

}