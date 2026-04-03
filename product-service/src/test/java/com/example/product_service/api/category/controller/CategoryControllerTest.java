package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.controller.dto.CategoryRequest.CreateRequest;
import com.example.product_service.api.category.controller.dto.CategoryRequest.MoveRequest;
import com.example.product_service.api.category.controller.dto.CategoryRequest.UpdateRequest;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
class CategoryControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("카테고리를 생성한다")
    @WithCustomMockUser
    void saveCategory() throws Exception {
        //given
        CreateRequest request = fixtureMonkey.giveMeBuilder(CreateRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        CategoryResponse response = fixtureMonkey.giveMeOne(CategoryResponse.class);
        given(categoryService.saveCategory(anyString(), nullable(Long.class), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/categories")
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
        CreateRequest request = fixtureMonkey.giveMeBuilder(CreateRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리를 생성할 수 없다")
    void saveCategory_unAuthentication() throws Exception {
        //given
        CreateRequest request = fixtureMonkey.giveMeBuilder(CreateRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCreateRequest")
    @DisplayName("카테고리 생성 요청 검증")
    @WithCustomMockUser
    void saveCategoryValidation(String description, CreateRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories"));
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    @WithCustomMockUser
    void updateCategory() throws Exception {
        //given
        CreateRequest request = fixtureMonkey.giveMeBuilder(CreateRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        CategoryResponse response = fixtureMonkey.giveMeOne(CategoryResponse.class);
        given(categoryService.updateCategory(anyLong(), anyString(), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("카테고리를 수정하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void updateCategoryWhenUserRole() throws Exception {
        //given
        UpdateRequest request = fixtureMonkey.giveMeBuilder(UpdateRequest.class)
                .set("name", "새 카티고리")
                .set("imagePath", "/test/new-image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(patch("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리를 수정할 수 없다")
    void updateCategory_unAuthentication() throws Exception {
        //given
        UpdateRequest request = fixtureMonkey.giveMeBuilder(UpdateRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(patch("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/1"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidUpdateRequest")
    @DisplayName("카테고리 수정 요청 검증")
    @WithCustomMockUser
    void updateCategoryValidation(String description, UpdateRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(patch("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/1"));
    }

    @Test
    @DisplayName("카테고리의 부모를 변경한다")
    @WithCustomMockUser
    void moveParent() throws Exception {
        //given
        MoveRequest request = fixtureMonkey.giveMeBuilder(MoveRequest.class)
                .set("parentId", 1L)
                .sample();
        CategoryResponse response = fixtureMonkey.giveMeOne(CategoryResponse.class);
        given(categoryService.moveParent(anyLong(), anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("카테고리 부모를 변경하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void moveParentWhenUserRole() throws Exception {
        //given
        MoveRequest request = fixtureMonkey.giveMeBuilder(MoveRequest.class)
                .set("parentId", 1L)
                .sample();
        //when
        //then
        mockMvc.perform(post("/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/1/move"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리 부모를 변경할 수 없다")
    void moveParent_unAuthentication() throws Exception {
        //given
        MoveRequest request = fixtureMonkey.giveMeBuilder(MoveRequest.class)
                .set("parentId", 1L)
                .sample();
        //when
        //then
        mockMvc.perform(post("/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/1/move"));
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    @WithCustomMockUser
    void deleteCategory() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("카테고리를 삭제하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void deleteCategoryWhenUserRole() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리를 삭제할 수 없다")
    void deleteCategory_unAuthentication() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/1"));
    }

    @Test
    @DisplayName("카테고리 트리를 조회한다")
    void getCategoryTree() throws Exception {
        //given
        List<CategoryTreeResponse> response = fixtureMonkey.giveMe(CategoryTreeResponse.class, 3);
        given(categoryService.getTree())
                        .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("카테고리 네비게이션을 조회한다")
    void getCategoryNavigation() throws Exception {
        //given
        CategoryNavigationResponse response = fixtureMonkey.giveMeOne(CategoryNavigationResponse.class);
        given(categoryService.getNavigation(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/categories/navigation/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory() throws Exception {
        //given
        CategoryResponse response = fixtureMonkey.giveMeOne(CategoryResponse.class);
        given(categoryService.getCategory(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    private static Stream<Arguments> provideInvalidCreateRequest() {
        return Stream.of(
                Arguments.of("카테고리 이름은 공백이 아닌 필수값이여야한다",
                        CreateRequest.builder()
                                .name(null)
                                .imagePath("/test/image.jpg")
                                .build(),
                        "name은 필수값입니다"
                ),
                Arguments.of("imagePath는 유효한 이미지 파일 형식 ('/'시작, 확장자 등)에 만족해야한다",
                        CreateRequest.builder()
                                .name("카테고리")
                                .imagePath("invalid-image-files")
                                .build(),
                        "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다")
        );
    }

    private static Stream<Arguments> provideInvalidUpdateRequest() {
        return Stream.of(
                Arguments.of("imagePath는 유효한 이미지 파일 형식 ('/'시작, 확장자 등)에 만족해야한다",
                        UpdateRequest.builder()
                                .name("변경된 카테고리")
                                .imagePath("invalid=image-files")
                                .build(),
                        "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"),
                Arguments.of("필드는 최소 하나는 존재해야한다",
                        UpdateRequest.builder()
                                .name(null)
                                .imagePath(null)
                                .build(),
                        "수정할 값이 하나는 존재해야합니다")
        );
    }
}
