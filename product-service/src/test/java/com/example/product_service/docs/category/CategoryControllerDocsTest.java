package com.example.product_service.docs.category;

import com.example.product_service.api.category.controller.CategoryController;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.api.category.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryControllerDocsTest extends RestDocsSupport {

    CategoryService categoryService = mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new CategoryController(categoryService);
    }

    @Test
    @DisplayName("카테고리 트리 구조 조회")
    void getCategoryTree() throws Exception {
        //given
        List<CategoryTreeResponse> categoryTreeResponses = mappingTreeResponse();
        given(categoryService.getTree())
                .willReturn(categoryTreeResponses);
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("category-get-tree",
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        beneathPath("[]").withSubsectionId("category"),
                                        fieldWithPath("id").description("카테고리 ID"),
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("parentId").description("부모 카테고리 ID"),
                                        fieldWithPath("depth").description("카테고리 깊이"),
                                        fieldWithPath("imageUrl").description("카테고리 이미지 URL"),
                                        subsectionWithPath("children").description("하위 카테고리 목록 (상위 구조와 동일)")
                                )
                        )
                );
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
        mockMvc.perform(get("/categories/navigation/{categoryId}", 2L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("category-get-navigation",
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("categoryId").description("조회할 카테고리 ID")
                                ),
                                responseFields(
                                        subsectionWithPath("current").description("요청 카테고리"),
                                        subsectionWithPath("ancestors").description("직계 카테고리"),
                                        subsectionWithPath("siblings").description("형제 카테고리"),
                                        subsectionWithPath("children").description("자식 카테고리")
                                )

                        )
                );
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory() throws Exception {
        //given
        CategoryResponse response = createCategoryResponse(1L, "카테고리", null, 1, "http://category.jpg");
        given(categoryService.getCategory(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "category-get",
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("categoryId").description("조회할 카테고리 ID")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("카테고리 ID"),
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("parentId").description("부모 카테고리 ID"),
                                        fieldWithPath("depth").description("카테고리 깊이"),
                                        fieldWithPath("imageUrl").description("카테고리 이미지 URL")
                                )

                        )
                );
    }

    private CategoryNavigationResponse createNavigation() {
        CategoryResponse electron = createCategoryResponse(1L, "전자기기", null, 1, "http://electron.jpg");
        CategoryResponse laptop = createCategoryResponse(2L, "노트북", 1L, 2, "http://laptop.jpg");
        CategoryResponse desktop = createCategoryResponse(3L, "데스크탑", 1L, 2, "http://desktop.jpg");
        CategoryResponse light = createCategoryResponse(4L, "경량 노트북", 2L, 3, "http://lightlaptop.jpg");
        CategoryResponse gaming = createCategoryResponse(5L, "게이밍 노트북", 2L, 3, "http://gaminglaptop.jpg");

        return  CategoryNavigationResponse.builder()
                .current(laptop)
                .ancestors(List.of(electron, laptop))
                .siblings(List.of(desktop))
                .children(List.of(light, gaming))
                .build();
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

    private CategoryResponse createCategoryResponse(Long id, String name, Long parentId, int depth,
                                                        String imageUrl) {

        return CategoryResponse.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .depth(depth)
                .imageUrl(imageUrl)
                .build();
    }
}
