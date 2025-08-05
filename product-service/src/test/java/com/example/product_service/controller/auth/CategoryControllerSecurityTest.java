package com.example.product_service.controller.auth;

import com.example.product_service.common.advice.CustomAccessDeniedHandler;
import com.example.product_service.common.advice.CustomAuthenticationEntryPoint;
import com.example.product_service.config.WebSecurity;
import com.example.product_service.controller.CategoryController;
import com.example.product_service.controller.util.UserRole;
import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.ModifyCategoryRequest;
import com.example.product_service.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.product_service.controller.util.SecurityTestHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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

    @Test
    @DisplayName("카테고리 생성 테스트-인증 에러")
    void createCategoryTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(BASE_PATH), createCategoryRequest(), null);
        verifyUnauthorizedResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("카테고리 생성 테스트-권한 부족")
    void createCategoryTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, post(BASE_PATH), createCategoryRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, BASE_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-인증 에러")
    void updateCategoryTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(CATEGORY_ID_PATH), createModifyCategoryRequest(), null);
        verifyUnauthorizedResponse(perform, CATEGORY_ID_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-권한 부족")
    void updateCategoryTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, patch(CATEGORY_ID_PATH), createModifyCategoryRequest(), UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, CATEGORY_ID_PATH);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-인증 에러")
    void deleteCategoryTest_UnAuthorized() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, delete(CATEGORY_ID_PATH), null, null);
        verifyUnauthorizedResponse(perform, CATEGORY_ID_PATH);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-권한 부족")
    void deleteCategoryTest_NoPermission() throws Exception {
        ResultActions perform =
                performWithAuthAndBody(mockMvc, delete(CATEGORY_ID_PATH), null, UserRole.ROLE_USER);
        verifyNoPermissionResponse(perform, CATEGORY_ID_PATH);
    }

    private CategoryRequest createCategoryRequest() {
        return new CategoryRequest("name", 1L, "http://test.jpg");
    }

    private ModifyCategoryRequest createModifyCategoryRequest() {
        return new ModifyCategoryRequest("name", 1L, "http://test.jpg");
    }
}