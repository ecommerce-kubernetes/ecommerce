package com.example.product_service.category.adapter.in.web;

import com.example.product_service.category.application.service.CategoryQueryService;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.category.application.service.dto.result.CategoryResultDeprecated;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.product_service.category.fixture.CategoryResultFixture.anRootCategoriesResult;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @MockitoBean
    private CategoryService categoryService;

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
        CategoryResultDeprecated.Tree parent = createTree(1L, "전자기기", null, 1, "/test/electron.jpg");
        CategoryResultDeprecated.Tree laptop = createTree(2L, "노트북", 1L, 2, "/test/laptop.jpg");
        CategoryResultDeprecated.Tree cellPhone = createTree(3L, "핸드폰", 1L, 2, "/test/cellPhone.jpg");
        parent.addChild(laptop);
        parent.addChild(cellPhone);
        given(categoryService.getTree()).willReturn(List.of(parent));

        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}/children", 1L))
                .andDo(print())
                .andExpect(status().isOk());

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

    private CategoryResultDeprecated.Tree createTree(Long id, String name, Long parentId, int depth, String imagePath) {
        return CategoryResultDeprecated.Tree.builder()
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
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("카테고리 트리를 조회한다")
    void getCategoryTree() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
