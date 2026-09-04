package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.support.fixture.FixtureMonkeyFactory;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.example.product_service.category.fixture.CategoryRequestFixture.anCreateCategoryRequest;
import static com.example.product_service.category.fixture.CategoryRequestFixture.anUpdateCategoryRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Import(TestSecurityConfig.class)
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
        CreateCategoryRequest request = anCreateCategoryRequest().build();
        Long categoryId = 1L;
        given(categoryService.saveCategory(any(CategoryCommand.Create.class)))
                .willReturn(categoryId);
        //when
        //then
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(String.valueOf(categoryId)));
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



    @Test
    @DisplayName("카테고리를 수정한다")
    @WithCustomMockUser
    void updateCategory() throws Exception {
        //given
        UpdateCategoryRequest request = anUpdateCategoryRequest().build();
        Long categoryId = 1L;
        given(categoryService.updateCategory(any(CategoryCommand.Update.class)))
                .willReturn(categoryId);
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(categoryId)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidUpdateRequest")
    @DisplayName("카테고리 수정 요청 검증")
    @WithCustomMockUser
    void updateCategoryValidation(String description, UpdateCategoryRequest request, String message) throws Exception {
        //given
        Long categoryId = 1L;
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/categories/" + categoryId));
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
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    @WithCustomMockUser
    void deleteCategory() throws Exception {
        //given
        Long categoryId = 1L;
        //when
        //then
        mockMvc.perform(delete("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(categoryService).deleteCategory(1L);
    }

    private static Stream<Arguments> provideInvalidCreateRequest() {
        return Stream.of(
                Arguments.of("이름이 누락되면 예외가 발생한다.",
                        anCreateCategoryRequest().name(null).build(),
                        "name은 필수값입니다"
                ),
                Arguments.of("이미지 경로가 유효하지 않으면 예외가 발생한다.",
                        anCreateCategoryRequest().imagePath("invalid_image_file").build(),
                        "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다")
        );
    }

    private static Stream<Arguments> provideInvalidUpdateRequest() {
        return Stream.of(
                Arguments.of("imagePath가 유효하지 않으면 예외가 발생한다.",
                        anUpdateCategoryRequest().imagePath("invalid-image-path").build(),
                        "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"),
                Arguments.of("수정값이 하나도 존재하지 않으면 예외가 발생한다.",
                        anUpdateCategoryRequest().name(null).imagePath(null).build(),
                        "이름 또는 이미지 경로 중 하나는 필수입니다.")
        );
    }
}