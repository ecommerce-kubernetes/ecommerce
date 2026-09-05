package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.application.service.CategoryQueryService;
import com.example.product_service.category.application.service.dto.result.*;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.product_service.category.fixture.CategoryResultFixture.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @Test
    @DisplayName("최상위 카테고리 목록을 조회한다")
    void getRootCategories() throws Exception {
        //given
        RootCategoriesResult roots = anRootCategoriesResult().build();
        given(categoryQueryService.getRoots()).willReturn(roots);
        CategoryResult root = roots.categories().getFirst();
        //when
        //then
        mockMvc.perform(get("/categories/roots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].id").value(root.id()))
                .andExpect(jsonPath("$.categories[0].name").value(root.name()))
                .andExpect(jsonPath("$.categories[0].imagePath").value(root.imagePath()))
                .andExpect(jsonPath("$.categories[0].isLeaf").value(root.isLeaf()));

    }

    @Test
    @DisplayName("카테고리의 자식 목록을 조회한다")
    void getCategoryChildren() throws Exception {
        //given
        ChildCategoriesResult children = anChildCategoriesResult().build();
        given(categoryQueryService.getChildren(anyLong())).willReturn(children);
        CategoryResult child = children.categories().getFirst();
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}/children", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].id").value(child.id()))
                .andExpect(jsonPath("$.categories[0].name").value(child.name()))
                .andExpect(jsonPath("$.categories[0].imagePath").value(child.imagePath()))
                .andExpect(jsonPath("$.categories[0].isLeaf").value(child.isLeaf()));
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory() throws Exception {
        //given
        DetailCategoryResult detail = anDetailCategoryResult().build();
        given(categoryQueryService.getDetail(anyLong())).willReturn(detail);
        CategoryResult pathCategory = detail.breadcrumb().getFirst();
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(detail.id()))
                .andExpect(jsonPath("$.name").value(detail.name()))
                .andExpect(jsonPath("$.depth").value(detail.depth()))
                .andExpect(jsonPath("$.isLeaf").value(detail.isLeaf()))
                .andExpect(jsonPath("$.breadcrumb").isArray())
                .andExpect(jsonPath("$.breadcrumb[0].id").value(pathCategory.id()))
                .andExpect(jsonPath("$.breadcrumb[0].name").value(pathCategory.name()))
                .andExpect(jsonPath("$.breadcrumb[0].depth").value(pathCategory.depth()));
    }

    @Test
    @DisplayName("카테고리 트리를 조회한다")
    void getCategoryTree() throws Exception {
        //given
        TreeCategoriesResult tree = anTreeCategoriesResult().build();
        given(categoryQueryService.getTree()).willReturn(tree);
        TreeCategoriesResult.TreeCategoryResult root = tree.categories().getFirst();
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].id").value(root.id()))
                .andExpect(jsonPath("$.categories[0].name").value(root.name()))
                .andExpect(jsonPath("$.categories[0].depth").value(root.depth()))
                .andExpect(jsonPath("$.categories[0].isLeaf").value(root.isLeaf()))
                .andExpect(jsonPath("$.categories[0].children").isArray());
    }
}
