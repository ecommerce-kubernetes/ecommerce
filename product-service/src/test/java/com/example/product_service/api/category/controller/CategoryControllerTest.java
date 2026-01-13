package com.example.product_service.api.category.controller;

import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.api.category.controller.dto.MoveCategoryRequest;
import com.example.product_service.api.category.controller.dto.UpdateCategoryRequest;
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
public class CategoryControllerTest extends ControllerTestSupport {

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
        CategoryRequest request = createCategoryRequest().build();
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
        CategoryRequest request = createCategoryRequest().build();
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
    void saveCategoryValidation(String description, CategoryRequest request, String message) throws Exception {
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
        UpdateCategoryRequest request = createUpdateCategoryRequest().build();
        CategoryResponse response = createCategoryResponse().name("새 카테고리").imageUrl("http://newCategory.jpg").build();
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
        UpdateCategoryRequest request = createUpdateCategoryRequest().build();
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
        UpdateCategoryRequest request = createUpdateCategoryRequest().build();
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
    void updateCategoryValidation(String description, UpdateCategoryRequest request, String message) throws Exception {
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
        MoveCategoryRequest request = createMoveCategoryRequest().build();
        CategoryResponse response = createCategoryResponse().parentId(2L).build();
        given(categoryService.moveParent(anyLong(), anyLong(), any()))
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
        MoveCategoryRequest request = createMoveCategoryRequest().build();
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
        MoveCategoryRequest request = createMoveCategoryRequest().build();
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

    @ParameterizedTest(name = "{0}")
    @DisplayName("카테고리 부모 변경 요청 검증")
    @MethodSource("provideInvalidMoveRequest")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void moveParentValidation(String description, MoveCategoryRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(jsonPath("code").value("VALIDATION"))
                .andExpect(jsonPath("message").value(message))
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
        List<CategoryTreeResponse> response = mappingTreeResponse();
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
        CategoryNavigationResponse response = createNavigation();
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
        CategoryResponse response = createCategoryResponse().build();
        given(categoryService.getCategory(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    private List<CategoryTreeResponse> mappingTreeResponse() {
        CategoryTreeResponse electron = createCategoryTreeResponse(1L, "전자기기", null, 1, "http://electron.jpg");
        CategoryTreeResponse laptop = createCategoryTreeResponse(3L, "노트북", 1L, 2, "http://laptop.jpg");
        CategoryTreeResponse cellPhone = createCategoryTreeResponse(4L, "핸드폰", 1L, 2, "http://cellPhone.jpg");
        electron.addChild(laptop);
        electron.addChild(cellPhone);

        CategoryTreeResponse food = createCategoryTreeResponse(2L, "식품", null, 1, "http://food.jpg");
        CategoryTreeResponse meat = createCategoryTreeResponse(5L, "육류", 2L, 2, "http://meat.jpg");
        CategoryTreeResponse vegetable = createCategoryTreeResponse(6L, "채소류", 2L, 2, "http://vegetable.jpg");
        food.addChild(meat);
        food.addChild(vegetable);
        return List.of(electron, food);
    }

    private CategoryTreeResponse createCategoryTreeResponse(Long id, String name, Long parentId, int depth,
                                                            String imageUrl) {

        return CategoryTreeResponse.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .depth(depth)
                .imageUrl(imageUrl)
                .build();
    }

    private CategoryNavigationResponse createNavigation() {
        CategoryResponse electron = createCategoryResponse().id(1L).name("전자기기").parentId(null).depth(1).imageUrl("http://electron.jpg").build();
        CategoryResponse laptop = createCategoryResponse().id(2L).name("노트북").parentId(1L).depth(2).imageUrl("http://laptop.jpg").build();
        CategoryResponse desktop = createCategoryResponse().id(3L).name("데스크탑").parentId(1L).depth(2).imageUrl("http://desktop.jpg").build();
        CategoryResponse light = createCategoryResponse().id(4L).name("경량 노트북").parentId(2L).depth(3).imageUrl("http://lightlaptop.jpg").build();
        CategoryResponse gaming = createCategoryResponse().id(5L).name("게이밍 노트북").parentId(2L).depth(3).imageUrl("http://gaminglaptop.jpg").build();

        return  CategoryNavigationResponse.builder()
                .current(laptop)
                .ancestors(List.of(electron, laptop))
                .siblings(List.of(desktop))
                .children(List.of(light, gaming))
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

    private static UpdateCategoryRequest.UpdateCategoryRequestBuilder createUpdateCategoryRequest() {
        return UpdateCategoryRequest.builder()
                .name("새 카테고리")
                .imageUrl("http://newCategory.jpg");
    }

    private static CategoryRequest.CategoryRequestBuilder createCategoryRequest() {
        return CategoryRequest.builder()
                .name("카테고리")
                .parentId(null)
                .imageUrl("http://category.jpg");
    }

    private MoveCategoryRequest.MoveCategoryRequestBuilder createMoveCategoryRequest() {
        return MoveCategoryRequest.builder()
                .parentId(2L);
    }

    private static Stream<Arguments> provideInvalidCreateRequest() {
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

    private static Stream<Arguments> provideInvalidUpdateRequest() {
        return Stream.of(
                Arguments.of("imageUrl 값은 URL 형식이여야 한다",
                        createUpdateCategoryRequest().imageUrl("invalid").build(),
                        "imageUrl 형식은 URL 형식이여야합니다"),
                Arguments.of("필드는 최소 하나는 존재해야한다",
                        createUpdateCategoryRequest().name(null).imageUrl(null).build(),
                        "수정할 값이 하나는 존재해야합니다")
        );
    }

    private static Stream<Arguments> provideInvalidMoveRequest() {
        return Stream.of(
                Arguments.of("parentId 가 있는데 isRoot가 true",
                        MoveCategoryRequest.builder().parentId(1L).isRoot(true).build(),
                        "parentId 와 isRoot 를 명확히 지정해야합니다"),
                Arguments.of("parentId 가 없는데 isRoot가 false",
                        MoveCategoryRequest.builder().parentId(null).isRoot(false).build(),
                        "parentId 와 isRoot 를 명확히 지정해야합니다"),
                Arguments.of("parentId 와 isRoot가 둘다 없음",
                        MoveCategoryRequest.builder().build(),
                        "parentId 와 isRoot 를 명확히 지정해야합니다")
        );
    }
}
