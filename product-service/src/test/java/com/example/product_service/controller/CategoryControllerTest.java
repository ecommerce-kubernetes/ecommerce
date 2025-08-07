package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.controller.util.ControllerTestHelper;
import com.example.product_service.controller.util.TestMessageUtil;
import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
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


import java.util.List;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    private static final String ICON_URL = "http://test.jpg";
    private static final String BASE_PATH = "/categories";
    private static final String ROOT_CATEGORY_PATH = BASE_PATH + "/root";
    private static final String CHILDREN_CATEGORY_PATH = BASE_PATH + "/1/children";
    private static final String HIERARCHY_CATEGORY_PATH = BASE_PATH + "/5/hierarchy";
    private static final String UPDATE_CATEGORY_PATH = BASE_PATH + "/2";

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

    @Test
    @DisplayName("카테고리 루트 조회 테스트-성공")
    void getRootCategoriesTest_success() throws Exception {
        List<CategoryResponse> response = List.of(new CategoryResponse(1L, "category1", null, ICON_URL),
                new CategoryResponse(2L, "category2", null, ICON_URL));
        when(service.getRootCategories()).thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, get(ROOT_CATEGORY_PATH), null);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("카테고리 자식 조회 테스트-성공")
    void getChildrenCategoriesTest_success() throws Exception {
        List<CategoryResponse> response = List.of(new CategoryResponse(2L, "childCategory1", 1L, ICON_URL),
                new CategoryResponse(3L, "childCategory2", 1L, ICON_URL));
        when(service.getChildrenCategoriesById(anyLong()))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, get(CHILDREN_CATEGORY_PATH), null);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("카테고리 자식 조회 테스트-실패(부모 카테고리 없음)")
    void getChildrenCategoriesTest_notFound() throws Exception {
        when(service.getChildrenCategoriesById(any()))
                .thenThrow(new NotFoundException(getMessage("category.notFound")));

        ResultActions perform = performWithBody(mockMvc, get(CHILDREN_CATEGORY_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("category.notFound"), CHILDREN_CATEGORY_PATH);
    }

    @Test
    @DisplayName("카테고리 계층 구조 조회 테스트-성공")
    void getHierarchyTest_success() throws Exception {
        CategoryHierarchyResponse response = new CategoryHierarchyResponse(createAncestors(), createLevelItems());

        when(service.getHierarchyByCategoryId(anyLong()))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, get(HIERARCHY_CATEGORY_PATH), null);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("카테고리 계층 구조 조회 테스트-실패(없음)")
    void getHierarchyTest_notFound() throws Exception {
        when(service.getHierarchyByCategoryId(anyLong()))
                .thenThrow(new NotFoundException(getMessage("category.notFound")));

        ResultActions perform = performWithBody(mockMvc, get(HIERARCHY_CATEGORY_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("category.notFound"), HIERARCHY_CATEGORY_PATH);
    }

    private List<CategoryResponse> createAncestors(){
        return List.of(new CategoryResponse(1L, "depth-1 Category1", null, ICON_URL),
                new CategoryResponse(3L, "depth-2 Category1", 1L, ICON_URL),
                new CategoryResponse(5L, "depth-3 Category1", 3L, ICON_URL));
    }

    private List<CategoryHierarchyResponse.LevelItem> createLevelItems(){
        return List.of(
                new CategoryHierarchyResponse.LevelItem(1,
                        List.of(new CategoryResponse(1L, "depth-1 Category1", null, ICON_URL),
                                new CategoryResponse(2L, "depth-1 Category1", null, ICON_URL))),
                new CategoryHierarchyResponse.LevelItem(2,
                        List.of(new CategoryResponse(3L, "depth-2 Category1", 1L, ICON_URL),
                                new CategoryResponse(4L, "depth-2 Category2", 1L, ICON_URL))),
                new CategoryHierarchyResponse.LevelItem(3,
                        List.of(new CategoryResponse(5L, "depth-3 Category1", 3L, ICON_URL),
                                new CategoryResponse(6L, "depth-3 Category2", 3L, ICON_URL)))
        );
    }

}