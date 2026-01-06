package com.example.product_service.docs.category;

import com.example.product_service.api.category.controller.ManagementCategoryController;
import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.api.category.controller.dto.MoveCategoryRequest;
import com.example.product_service.api.category.controller.dto.UpdateCategoryRequest;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ManagementCategoryControllerDocsTest extends RestDocsSupport {

    private CategoryService categoryService = Mockito.mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new ManagementCategoryController(categoryService);
    }

    @Test
    @DisplayName("카테고리를 저장한다")
    void saveCategory() throws Exception {
        //given
        CategoryRequest request = createCategoryRequest();
        CategoryResponse response = createCategoryResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.saveCategory(any()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/management/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("create-category",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                requestFields(
                                        fieldWithPath("name").description("카테고리 이름").optional(),
                                        fieldWithPath("parentId").description("부모 카테고리 ID").type("Number"),
                                        fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("카테고리 ID"),
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("parentId").description("부모 카테고리 ID").type("Number"),
                                        fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
                                )
                        )
                );
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    void updateCategory() throws Exception {
        //given
        UpdateCategoryRequest request = createUpdateCategoryRequest();
        CategoryResponse response = createCategoryResponse().name("새 카테고리").imageUrl("http://newCategory.jpg").build();
        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.updateCategory(anyLong(), anyString(), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/management/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("update-category",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                requestFields(
                                        fieldWithPath("name").description("변경할 카테고리 이름"),
                                        fieldWithPath("imageUrl").description("변경할 카테고리 아이콘 URL")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("카테고리 ID"),
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("parentId").description("부모 카테고리 ID").type("Number"),
                                        fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
                                )
                        )
                );
    }

    @Test
    @DisplayName("카테고리의 부모를 변경한다")
    void moveParent() throws Exception {
        //given
        MoveCategoryRequest request = createMoveCategoryRequest();
        CategoryResponse response = createCategoryResponse().parentId(2L).build();
        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.moveParent(anyLong(), anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/management/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("moveParent-category",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),
                                requestFields(
                                        fieldWithPath("parentId").description("이동할 부모 카테고리 ID").optional()
                                ),
                                responseFields(
                                        fieldWithPath("id").description("카테고리 ID"),
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("parentId").description("부모 카테고리 ID").type("Number"),
                                        fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
                                )
                        )
                );
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    void deleteCategory() throws Exception {
        //given
        HttpHeaders adminHeader = createAdminHeader();
        willDoNothing().given(categoryService).deleteCategoryById(anyLong());
        //when
        //then
        mockMvc.perform(delete("/management/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("delete-category",
                                preprocessRequest(prettyPrint()),
                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                )
                        )
                );
    }

    private CategoryRequest createCategoryRequest() {
        return CategoryRequest.builder()
                .name("카테고리")
                .parentId(null)
                .imageUrl("http://category.jpg")
                .build();

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
                .imageUrl("http://category.jpg");
    }

    private HttpHeaders createAdminHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", "ROLE_ADMIN");
        return headers;
    }
}
