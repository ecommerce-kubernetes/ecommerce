package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.ErrorResponseEntityFactory;
import com.example.product_service.config.TestConfig;
import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.dto.response.category.CategoryHierarchyResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.api.category.service.CategoryService;
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

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.common.MessagePath.CONFLICT;
import static com.example.product_service.util.ControllerTestHelper.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeprecatedCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
class DeprecatedCategoryControllerTest {

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
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
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
    @DisplayName("카테고리 수정 테스트-실패(검증)")
    void updateCategoryTest_validation() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("", 1L, ICON_URL);

        ResultActions perform = performWithBody(mockMvc, patch(UPDATE_CATEGORY_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), UPDATE_CATEGORY_PATH);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-성공")
    void deleteCategoryTest_success() throws Exception {
        doNothing().when(service).deleteCategory(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(DELETE_CATEGORY_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("카테고리 삭제 테스트-실패(없음)")
    void deleteCategoryTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)))
                .when(service).deleteCategory(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(DELETE_CATEGORY_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND),DELETE_CATEGORY_PATH);
    }

    private List<CategoryResponse> createAncestors(){
        return List.of(new CategoryResponse(1L, "depth-1 Category1", null,1, ICON_URL),
                new CategoryResponse(3L, "depth-2 Category1", 1L,1, ICON_URL),
                new CategoryResponse(5L, "depth-3 Category1", 3L,1, ICON_URL));
    }

    private List<CategoryHierarchyResponse.LevelItem> createLevelItems(){
        return List.of(
                new CategoryHierarchyResponse.LevelItem(1,
                        List.of(new CategoryResponse(1L, "depth-1 Category1", null,1, ICON_URL),
                                new CategoryResponse(2L, "depth-1 Category1", null, 1,ICON_URL))),
                new CategoryHierarchyResponse.LevelItem(2,
                        List.of(new CategoryResponse(3L, "depth-2 Category1", 1L,1, ICON_URL),
                                new CategoryResponse(4L, "depth-2 Category2", 1L,1, ICON_URL))),
                new CategoryHierarchyResponse.LevelItem(3,
                        List.of(new CategoryResponse(5L, "depth-3 Category1", 3L,1, ICON_URL),
                                new CategoryResponse(6L, "depth-3 Category2", 3L,1, ICON_URL)))
        );
    }

}