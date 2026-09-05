package com.example.product_service.docs.category;

import com.example.product_service.category.adapter.in.web.CategoryController;
import com.example.product_service.category.application.service.CategoryQueryService;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.result.CategoryResultDeprecated;
import com.example.product_service.category.application.service.dto.result.ChildCategoriesResult;
import com.example.product_service.category.application.service.dto.result.RootCategoriesResult;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.example.product_service.category.fixture.CategoryResultFixture.anChildCategoriesResult;
import static com.example.product_service.category.fixture.CategoryResultFixture.anRootCategoriesResult;
import static com.example.product_service.docs.descriptor.CategoryDescriptor.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerDocsTest extends RestDocsSupport {

    CategoryQueryService categoryQueryService = Mockito.mock(CategoryQueryService.class);
    CategoryService categoryService = Mockito.mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new CategoryController(categoryQueryService, categoryService);
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
        CategoryResultDeprecated.Navigation result = createNavigation();
        given(categoryService.getNavigation(anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 2L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "categories/detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(detailResponse())
                ));
    }

    @Test
    @DisplayName("카테고리 트리 구조 조회")
    void getCategoryTree() throws Exception {
        //given
        List<CategoryResultDeprecated.Tree> results = mappingTreeResponse();
        given(categoryService.getTree()).willReturn(results);
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

    private CategoryResultDeprecated.Navigation createNavigation() {
        CategoryResultDeprecated.Detail electron = createCategoryResponse().id(1L).name("전자기기").parentId(null).depth(1).imagePath("/test/electron.jpg").build();
        CategoryResultDeprecated.Detail laptop = createCategoryResponse().id(2L).name("노트북").parentId(1L).depth(2).imagePath("/test/laptop.jpg").build();
        CategoryResultDeprecated.Detail desktop = createCategoryResponse().id(3L).name("데스크탑").parentId(1L).depth(2).imagePath("/test/desktop.jpg").build();
        CategoryResultDeprecated.Detail light = createCategoryResponse().id(4L).name("경량 노트북").parentId(2L).depth(3).imagePath("/test/lightLaptop.jpg").build();
        CategoryResultDeprecated.Detail gaming = createCategoryResponse().id(5L).name("게이밍 노트북").parentId(2L).depth(3).imagePath("/test/gamingLaptop.jpg").build();

        return CategoryResultDeprecated.Navigation.builder()
                .current(laptop)
                .path(List.of(electron, laptop))
                .siblings(List.of(desktop))
                .children(List.of(light, gaming))
                .build();
    }

    private List<CategoryResultDeprecated.Tree> mappingTreeResponse() {
        CategoryResultDeprecated.Tree electron = createCategoryTreeResponse(1L, "전자기기", null, 1, "/test/electron.jpg");
        CategoryResultDeprecated.Tree laptop = createCategoryTreeResponse(3L, "노트북", 1L, 2, "/test/laptop.jpg");
        CategoryResultDeprecated.Tree cellPhone = createCategoryTreeResponse(4L, "핸드폰", 1L, 2, "/test/cellPhone.jpg");
        electron.addChild(laptop);
        electron.addChild(cellPhone);

        CategoryResultDeprecated.Tree food = createCategoryTreeResponse(2L, "식품", null, 1, "/test/food.jpg");
        CategoryResultDeprecated.Tree meat = createCategoryTreeResponse(5L, "육류", 2L, 2, "/test/meat.jpg");
        CategoryResultDeprecated.Tree vegetable = createCategoryTreeResponse(6L, "채소류", 2L, 2, "/test/vegetable.jpg");
        food.addChild(meat);
        food.addChild(vegetable);
        return List.of(electron, food);
    }

    private CategoryResultDeprecated.Tree createCategoryTreeResponse(Long id, String name, Long parentId, int depth,
                                                                     String imagePath) {
        return CategoryResultDeprecated.Tree.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .depth(depth)
                .imagePath(imagePath)
                .build();
    }

    private CategoryResultDeprecated.Detail.DetailBuilder createCategoryResponse() {
        return CategoryResultDeprecated.Detail.builder()
                .id(1L)
                .name("카테고리")
                .parentId(null)
                .depth(1)
                .imagePath("/test/category.jpg");
    }
}
