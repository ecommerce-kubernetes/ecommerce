package com.example.product_service.docs.category;

import com.example.product_service.api.category.controller.ManagementCategoryController;
import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.dto.response.category.CategoryResponse;
import com.example.product_service.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ManagementCategoryControllerDocsTest extends RestDocsSupport {

    private CategoryService categoryService = Mockito.mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new ManagementCategoryController(categoryService);
    }

    @Test
    @DisplayName("")
    void saveCategory() throws Exception {
        //given
        CategoryRequest request = createCategoryRequest();
        CategoryResponse response = createCategoryResponse();
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
                                )
                        )
                );
    }

    private CategoryRequest createCategoryRequest() {
        return CategoryRequest.builder()
                .name("카테고리")
                .parentId(null)
                .iconUrl("http://category.jpg")
                .build();

    }

    private CategoryResponse createCategoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("카테고리")
                .parentId(null)
                .iconUrl("http://category.jpg")
                .build();

    }

    private HttpHeaders createAdminHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", "ROLE_ADMIN");
        return headers;
    }
}
