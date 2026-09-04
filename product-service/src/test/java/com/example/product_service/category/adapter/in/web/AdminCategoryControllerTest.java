package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryIdResponse;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.common.security.model.UserRole;
import com.example.product_service.support.fixture.FixtureMonkeyFactory;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AdminCategoryController.class)
class AdminCategoryControllerTest {
    protected final FixtureMonkey fixtureMonkey = FixtureMonkeyFactory.get;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected CategoryService categoryService;

    @Test
    @DisplayName("카테고리를 생성한다")
    @WithCustomMockUser
    void saveCategory() throws Exception {
        //given
        CreateCategoryRequest request = fixtureMonkey.giveMeBuilder(CreateCategoryRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        CategoryResult.Detail result = fixtureMonkey.giveMeOne(CategoryResult.Detail.class);
        assert result != null;

        given(categoryService.saveCategory(any(CategoryCommand.Create.class)))
                .willReturn(result);
        CategoryIdResponse response = CategoryIdResponse.from(result);
        //when
        //then
        mockMvc.perform(post("/admin/categories")
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
        CreateCategoryRequest request = fixtureMonkey.giveMeBuilder(CreateCategoryRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리를 생성할 수 없다")
    void saveCategory_unAuthentication() throws Exception {
        //given
        CreateCategoryRequest request = fixtureMonkey.giveMeBuilder(CreateCategoryRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCreateRequest")
    @DisplayName("카테고리 생성 요청 검증")
    @WithCustomMockUser
    void saveCategoryValidation(String description, CreateCategoryRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories"));
    }

    private static Stream<Arguments> provideInvalidCreateRequest() {
        return Stream.of(
                Arguments.of("카테고리 이름은 공백이 아닌 필수값이여야한다",
                        CreateCategoryRequest.builder()
                                .name(null)
                                .imagePath("/test/image.jpg")
                                .build(),
                        "name은 필수값입니다"
                ),
                Arguments.of("imagePath는 유효한 이미지 파일 형식 ('/'시작, 확장자 등)에 만족해야한다",
                        CreateCategoryRequest.builder()
                                .name("카테고리")
                                .imagePath("invalid-image-files")
                                .build(),
                        "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다")
        );
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    @WithCustomMockUser
    void updateCategory() throws Exception {
        //given
        CreateCategoryRequest request = fixtureMonkey.giveMeBuilder(CreateCategoryRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        CategoryResult.Detail result = fixtureMonkey.giveMeOne(CategoryResult.Detail.class);
        given(categoryService.updateCategory(any(CategoryCommand.Update.class)))
                .willReturn(result);
        assert result != null;
        CategoryIdResponse response = CategoryIdResponse.from(result);
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", 1L)
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
        UpdateCategoryRequest request = fixtureMonkey.giveMeBuilder(UpdateCategoryRequest.class)
                .set("name", "새 카티고리")
                .set("imagePath", "/test/new-image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리를 수정할 수 없다")
    void updateCategory_unAuthentication() throws Exception {
        //given
        UpdateCategoryRequest request = fixtureMonkey.giveMeBuilder(UpdateCategoryRequest.class)
                .set("name", "카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/1"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidUpdateRequest")
    @DisplayName("카테고리 수정 요청 검증")
    @WithCustomMockUser
    void updateCategoryValidation(String description, UpdateCategoryRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/1"));
    }

    private static Stream<Arguments> provideInvalidUpdateRequest() {
        return Stream.of(
                Arguments.of("imagePath는 유효한 이미지 파일 형식 ('/'시작, 확장자 등)에 만족해야한다",
                        UpdateCategoryRequest.builder()
                                .name("변경된 카테고리")
                                .imagePath("invalid=image-files")
                                .build(),
                        "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"),
                Arguments.of("필드는 최소 하나는 존재해야한다",
                        UpdateCategoryRequest.builder()
                                .name(null)
                                .imagePath(null)
                                .build(),
                        "수정할 값이 하나는 존재해야합니다")
        );
    }

    @Test
    @DisplayName("카테고리의 부모를 변경한다")
    @WithCustomMockUser
    void moveParent() throws Exception {
        //given
        MoveCategoryRequest request = fixtureMonkey.giveMeBuilder(MoveCategoryRequest.class)
                .set("newParentId", 1L)
                .sample();
        CategoryResult.Detail result = fixtureMonkey.giveMeOne(CategoryResult.Detail.class);
        given(categoryService.moveParent(anyLong(), anyLong())).willReturn(result);
        assert result != null;
        CategoryIdResponse response = CategoryIdResponse.from(result);
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}/move", 1L)
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
        MoveCategoryRequest request = fixtureMonkey.giveMeBuilder(MoveCategoryRequest.class)
                .set("newParentId", 1L)
                .sample();
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/1/move"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리 부모를 변경할 수 없다")
    void moveParent_unAuthentication() throws Exception {
        //given
        MoveCategoryRequest request = fixtureMonkey.giveMeBuilder(MoveCategoryRequest.class)
                .set("newParentId", 1L)
                .sample();
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/1/move"));
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    @WithCustomMockUser
    void deleteCategory() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/admin/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @DisplayName("카테고리를 삭제하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void deleteCategoryWhenUserRole() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/admin/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(jsonPath("code").value("FORBIDDEN"))
                .andExpect(jsonPath("message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/1"));
    }

    @Test
    @DisplayName("로그인 하지 않은 유저는 카테고리를 삭제할 수 없다")
    void deleteCategory_unAuthentication() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(delete("/admin/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/1"));
    }
}