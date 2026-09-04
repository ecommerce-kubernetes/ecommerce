package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.adapter.in.web.dto.response.CategoryDetailResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryListResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryTreeListResponse;
import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
class CategoryControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("최상위 카테고리 목록을 조회한다")
    void getRootCategories() throws Exception {
        //given
        List<CategoryResult.Tree> results = fixtureMonkey.giveMe(CategoryResult.Tree.class, 3);
        given(categoryService.getTree()).willReturn(results);
        CategoryListResponse response = CategoryListResponse.fromRoots(results);
        //when
        //then
        mockMvc.perform(get("/categories/roots"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("카테고리의 자식 목록을 조회한다")
    void getCategoryChildren() throws Exception {
        //given
        CategoryResult.Tree parent = createTree(1L, "전자기기", null, 1, "/test/electron.jpg");
        CategoryResult.Tree laptop = createTree(2L, "노트북", 1L, 2, "/test/laptop.jpg");
        CategoryResult.Tree cellPhone = createTree(3L, "핸드폰", 1L, 2, "/test/cellPhone.jpg");
        parent.addChild(laptop);
        parent.addChild(cellPhone);
        given(categoryService.getTree()).willReturn(List.of(parent));
        CategoryListResponse response = CategoryListResponse.fromChildren(parent);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}/children", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리의 자식을 조회하면 예외가 발생한다")
    void getCategoryChildren_whenNotFound_thenThrowException() throws Exception {
        //given
        given(categoryService.getTree()).willReturn(List.of());
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}/children", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("code").value("CATEGORY_001"))
                .andExpect(jsonPath("message").value("카테고리를 찾을 수 없습니다"))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/categories/999/children"));
    }

    private CategoryResult.Tree createTree(Long id, String name, Long parentId, int depth, String imagePath) {
        return CategoryResult.Tree.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .depth(depth)
                .imagePath(imagePath)
                .build();
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory() throws Exception {
        //given
        CategoryResult.Navigation result = fixtureMonkey.giveMeOne(CategoryResult.Navigation.class);
        assert result != null;
        given(categoryService.getNavigation(anyLong())).willReturn(result);
        CategoryDetailResponse response = CategoryDetailResponse.from(result);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("카테고리 트리를 조회한다")
    void getCategoryTree() throws Exception {
        //given
        List<CategoryResult.Tree> results = fixtureMonkey.giveMe(CategoryResult.Tree.class, 3);
        given(categoryService.getTree()).willReturn(results);
        CategoryTreeListResponse response = CategoryTreeListResponse.from(results);
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

}
