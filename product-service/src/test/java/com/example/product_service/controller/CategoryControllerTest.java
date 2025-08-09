package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.ErrorResponseEntityFactory;
import com.example.product_service.controller.util.MessagePath;
import com.example.product_service.dto.request.category.CategoryRequest;
import com.example.product_service.dto.request.category.UpdateCategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.DuplicateResourceException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.util.List;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static com.example.product_service.controller.util.MessagePath.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ErrorResponseEntityFactory.class)
class CategoryControllerTest {

    private static final String ICON_URL = "http://test.jpg";
    private static final String BASE_PATH = "/categories";
    private static final String ROOT_CATEGORY_PATH = BASE_PATH + "/root";
    private static final String CHILDREN_CATEGORY_PATH = BASE_PATH + "/1/children";
    private static final String HIERARCHY_CATEGORY_PATH = BASE_PATH + "/5/hierarchy";
    private static final String UPDATE_CATEGORY_PATH = BASE_PATH + "/2";
    private static final String DELETE_CATEGORY_PATH = BASE_PATH + "/1";

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    CategoryService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(NOT_FOUND)).thenReturn("NotFound");
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(MessagePath.BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(MessagePath.CONFLICT)).thenReturn("Conflict");
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
                getMessage(BAD_REQUEST), getMessage(BAD_REQUEST_VALIDATION), BASE_PATH);

        perform
                .andExpect(jsonPath("$.errors", hasSize(2)));

        perform
                .andExpect(jsonPath("$.errors[*].fieldName", containsInAnyOrder("name", "iconUrl")))
                .andExpect(jsonPath("$.errors[*].message",
                        containsInAnyOrder(getMessage(NOT_BLANK), getMessage(INVALID_URL_MESSAGE))));
    }

    @Test
    @DisplayName("카테고리 생성 테스트-실패(부모 카테고리 없음)")
    void createCategoryTest_notFound() throws Exception {
        CategoryRequest request = new CategoryRequest("childCategory", 1L, ICON_URL);
        when(service.saveCategory(any(CategoryRequest.class)))
                .thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND), BASE_PATH);

    }

    @Test
    @DisplayName("카테고리 생성 테스트-실패(중복)")
    void createCategoryTest_conflict() throws Exception {
        CategoryRequest request = new CategoryRequest("duplicated", 1L, ICON_URL);

        when(service.saveCategory(any(CategoryRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage(CATEGORY_CONFLICT)));
        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT),
                getMessage(CATEGORY_CONFLICT), BASE_PATH);
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
                .thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, get(CHILDREN_CATEGORY_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND), CHILDREN_CATEGORY_PATH);
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
                .thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, get(HIERARCHY_CATEGORY_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND), HIERARCHY_CATEGORY_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-성공")
    void updateCategoryTest_success() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", 1L, ICON_URL);
        CategoryResponse response = new CategoryResponse(2L, "updated", 1L, ICON_URL);
        when(service.updateCategoryById(anyLong(), any(UpdateCategoryRequest.class)))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, patch(UPDATE_CATEGORY_PATH), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(검증)")
    void updateCategoryTest_validation() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("", 1L, ICON_URL);

        ResultActions perform = performWithBody(mockMvc, patch(UPDATE_CATEGORY_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), UPDATE_CATEGORY_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(없음)")
    void updateCategoryTest_notFound() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", 1L, ICON_URL);
        when(service.updateCategoryById(anyLong(), any(UpdateCategoryRequest.class)))
                .thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, patch(UPDATE_CATEGORY_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND), UPDATE_CATEGORY_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(중복)")
    void updateCategoryTest_conflict() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("duplicate", 1L, ICON_URL);
        when(service.updateCategoryById(anyLong(), any(UpdateCategoryRequest.class)))
                .thenThrow(new DuplicateResourceException(getMessage(CATEGORY_CONFLICT)));

        ResultActions perform = performWithBody(mockMvc, patch(UPDATE_CATEGORY_PATH), request);
        verifyErrorResponse(perform, status().isConflict(), getMessage(CONFLICT),
                getMessage(CATEGORY_CONFLICT), UPDATE_CATEGORY_PATH);
    }

    @Test
    @DisplayName("카테고리 수정 테스트-실패(부모 카테고리 ID를 자신으로 할당)")
    void updateCategoryTest_badRequest() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("updated", 2L, ICON_URL);
        when(service.updateCategoryById(anyLong(), any(UpdateCategoryRequest.class)))
                .thenThrow(new BadRequestException(getMessage(CATEGORY_BAD_REQUEST)));
        ResultActions perform = performWithBody(mockMvc, patch(UPDATE_CATEGORY_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(CATEGORY_BAD_REQUEST), UPDATE_CATEGORY_PATH);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-성공")
    void deleteCategoryTest_success() throws Exception {
        doNothing().when(service).deleteCategoryById(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(DELETE_CATEGORY_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-실패(없음)")
    void deleteCategoryTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)))
                .when(service).deleteCategoryById(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(DELETE_CATEGORY_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND),DELETE_CATEGORY_PATH);
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