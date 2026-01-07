package com.example.product_service.api.category;

import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.api.category.controller.dto.MoveCategoryRequest;
import com.example.product_service.api.category.controller.dto.UpdateCategoryRequest;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.api.support.ControllerTestSupport;
import com.example.product_service.api.support.security.annotation.WithCustomMockUser;
import com.example.product_service.api.support.security.config.TestSecurityConfig;
import com.example.product_service.config.TestConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({TestConfig.class, TestSecurityConfig.class})
public class ManagementCategoryControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("카테고리를 생성한다")
    @WithCustomMockUser
    void saveCategory() throws Exception {
        //given
        CategoryRequest request = createCategoryRequest().build();
        CategoryResponse response = createCategoryResponse().build();
        given(categoryService.saveCategory(anyString(), nullable(Long.class), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/management/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("카테고리를 생성하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void saveCategoryWithUserRole() throws Exception {
        //given
        CategoryRequest request = createCategoryRequest().build();
        //when
        //then
        mockMvc.perform(post("/management/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/management/categories"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidRequest")
    @DisplayName("카테고리 생성 요청 검증")
    @WithCustomMockUser
    void saveCategoryValidation(String description, CategoryRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/management/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/management/categories"));
    }

    private static CategoryRequest.CategoryRequestBuilder createCategoryRequest() {
        return CategoryRequest.builder()
                .name("카테고리")
                .parentId(null)
                .imageUrl("http://category.jpg");
    }

    private UpdateCategoryRequest createUpdateCategoryRequest() {
        return UpdateCategoryRequest.builder()
                .name("새 카테고리")
                .imageUrl("http://newCategory.jpg")
                .build();
    }

    private MoveCategoryRequest createMoveCategoryRequest() {
        return MoveCategoryRequest.builder()
                .parentId(2L)
                .build();
    }

    private CategoryResponse.CategoryResponseBuilder createCategoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("카테고리")
                .parentId(null)
                .depth(1)
                .imageUrl("http://category.jpg");
    }

    private static Stream<Arguments> provideInvalidRequest() {
        return Stream.of(
                Arguments.of("카테고리 이름은 공백이 아닌 필수값이여야한다",
                        createCategoryRequest().name("").build(),
                        "name은 필수값입니다"
                ),
                Arguments.of("imageUrl 값은 URL 형식이여야 한다",
                        createCategoryRequest().imageUrl("invalid").build(),
                        "imageUrl 형식은 URL 형식이여야합니다")
        );
    }
}
