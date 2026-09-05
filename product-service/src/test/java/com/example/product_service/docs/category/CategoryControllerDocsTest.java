package com.example.product_service.docs.category;

import com.example.product_service.category.adapter.in.web.CategoryController;
import com.example.product_service.category.application.service.CategoryQueryService;
import com.example.product_service.category.application.service.dto.result.ChildCategoriesResult;
import com.example.product_service.category.application.service.dto.result.DetailCategoryResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.example.product_service.category.application.service.dto.result.TreeCategoriesResult;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.example.product_service.category.fixture.CategoryResultFixture.*;
import static com.example.product_service.docs.descriptor.CategoryDescriptor.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerDocsTest extends RestDocsSupport {

    private CategoryQueryService categoryQueryService = Mockito.mock(CategoryQueryService.class);

    @Override
    protected Object initController() {
        return new CategoryController(categoryQueryService);
    }

    @Test
    @DisplayName("최상위 카테고리 목록을 조회한다")
    void getRootCategories() throws Exception {
        //given
        RootCategoriesResult roots = anRootCategoriesResult().build();
        given(categoryQueryService.getRoots()).willReturn(roots);
        //when
        //then
        mockMvc.perform(get("/categories/roots"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "categories/roots",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(rootsResponse())
                ));
    }

    @Test
    @DisplayName("카테고리의 자식 목록을 조회한다")
    void getCategoryChildren() throws Exception {
        //given
        Long parentId = 1L;
        ChildCategoriesResult children = anChildCategoriesResult().build();
        given(categoryQueryService.getChildren(anyLong())).willReturn(children);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}/children", parentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "categories/children",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("categoryId").description("하위 카테고리를 조회할 상위 카테고리 ID")),
                        responseFields(childResponse())
                ));
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory() throws Exception {
        //given
        DetailCategoryResult detail = anDetailCategoryResult().build();
        given(categoryQueryService.getDetail(anyLong())).willReturn(detail);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 3L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "categories/detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("categoryId").description("조회할 카테고리 ID")),
                        responseFields(detailResponse())
                ));
    }

    @Test
    @DisplayName("카테고리 트리 구조 조회")
    void getCategoryTree() throws Exception {
        //given
        TreeCategoriesResult tree = anTreeCategoriesResult().build();
        given(categoryQueryService.getTree()).willReturn(tree);
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "categories/tree",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(treeResponse())
                ));
    }
}
