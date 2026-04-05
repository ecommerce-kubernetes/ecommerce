package com.example.product_service.docs.category;

import com.example.product_service.api.category.controller.CategoryController;
import com.example.product_service.api.category.controller.dto.request.CategoryRequest;
import com.example.product_service.api.category.controller.dto.response.CategoryResponse;
import com.example.product_service.api.category.service.CategoryService;
import com.example.product_service.api.category.service.dto.command.CategoryCommand;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResult;
import com.example.product_service.api.category.service.dto.result.CategoryResult;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResult;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.docs.descriptor.CategoryDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerDocsTest extends RestDocsSupport {

    CategoryService categoryService = mock(CategoryService.class);

    @Override
    protected String getTag() {
        return "Category";
    }
    @Override
    protected Object initController() {
        return new CategoryController(categoryService);
    }

    @Test
    @DisplayName("카테고리를 저장한다")
    void saveCategory() throws Exception {
        //given
        CategoryRequest.Create request = CategoryRequest.Create.builder()
                .name("카테고리")
                .parentId(null)
                .imagePath("/test/image.jpg")
                .build();
        CategoryResult.Detail result = fixtureMonkey.giveMeBuilder(CategoryResult.Detail.class)
                .set("id", 1L)
                .set("name", "카테고리")
                .set("parentId", null)
                .set("depth", 1)
                .set("imagePath", "/test/image.jpg")
                .sample();
        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.saveCategory(any(CategoryCommand.Create.class)))
                .willReturn(result);
        assert result != null;
        CategoryResponse.Detail response = CategoryResponse.Detail.from(result);
        //when
        //then
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument(
                        "01-category-01-create",
                        "카테고리 생성",
                        "새로운 카테고리를 생성합니다",
                        CategoryDescriptor.getCreateRequest(),
                        CategoryDescriptor.getCategoryResponse()
                ));
    }

    @Test
    @DisplayName("카테고리 트리 구조 조회")
    void getCategoryTree() throws Exception {
        //given
        List<CategoryTreeResult> results = mappingTreeResponse();
        given(categoryService.getTree()).willReturn(results);
        List<CategoryResponse.Tree> response = results.stream().map(CategoryResponse.Tree::from).toList();
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createPublicDocument("01-category-02-get-tree",
                                "카테고리 트리 조회",
                                "전체 카테고리를 트리 구조로 조회합니다",
                                CategoryDescriptor.getTreeResponse())
                );
    }

    @Test
    @DisplayName("카테고리 네비게이션을 조회한다")
    void getCategoryNavigation() throws Exception {
        //given
        CategoryNavigationResult result = createNavigation();
        given(categoryService.getNavigation(anyLong()))
                .willReturn(result);
        CategoryResponse.Navigation response = CategoryResponse.Navigation.from(result);
        //when
        //then
        mockMvc.perform(get("/categories/navigation/{categoryId}", 2L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createPublicDocument("01-category-03-get-navigation",
                        "카테고리 네비게이션 조회",
                        "특정 카테고리의 네비게이션 구조를 조회합니다",
                        CategoryDescriptor.getNavigationResponse(),
                        parameterWithName("categoryId").description("조회할 카테고리 ID"))
                );
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory() throws Exception {
        //given
        CategoryResult result = fixtureMonkey.giveMeBuilder(CategoryResult.class)
                .set("id", 2L)
                .set("name", "카테고리")
                .set("parentId", 1L)
                .set("depth", 2)
                .set("imageUrl", "/test/image.jpg")
                .sample();
        given(categoryService.getCategory(anyLong()))
                .willReturn(result);
        assert result != null;
        CategoryResponse.Detail response = CategoryResponse.Detail.from(result);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createPublicDocument("01-category-04-get",
                        "카테고리 단건 조회",
                        "ID로 특정 카테고리의 상세 정보 조회",
                        CategoryDescriptor.getCategoryResponse(),
                        parameterWithName("categoryId").description("조회할 카테고리 ID"))
                );
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    void updateCategory() throws Exception {
        //given
        CategoryRequest.Update request = fixtureMonkey.giveMeBuilder(CategoryRequest.Update.class)
                .set("name", "새 카테고리")
                .set("imagePath", "/test/image.jpg")
                .sample();
        CategoryResult.Detail result = fixtureMonkey.giveMeBuilder(CategoryResult.Detail.class)
                .set("id", 1L)
                .set("name", "새 카테고리")
                .set("parentId", null)
                .set("depth", 1)
                .set("imagePath", "/test/image.jpg")
                .sample();
        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.updateCategory(any(CategoryCommand.Update.class))).willReturn(result);
        assert result != null;
        CategoryResponse.Detail response = CategoryResponse.Detail.from(result);
        //when
        //then
        mockMvc.perform(patch("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("01-category-05-update",
                        "카테고리 수정",
                        "ID로 특정 카테고리의 기본 정보 수정",
                        CategoryDescriptor.getUpdateRequest(),
                        CategoryDescriptor.getCategoryResponse(),
                        parameterWithName("categoryId").description("조회할 카테고리 ID"))
                );
    }

    @Test
    @DisplayName("카테고리의 부모를 변경한다")
    void moveParent() throws Exception {
        //given
        CategoryRequest.MoveRequest request = fixtureMonkey.giveMeBuilder(CategoryRequest.MoveRequest.class)
                .set("parentId", 1L)
                .sample();

        CategoryResult result = fixtureMonkey.giveMeBuilder(CategoryResult.class)
                .set("id", 2L)
                .set("name", "자식 카테고리")
                .set("parentId", 1L)
                .set("depth", 2)
                .set("imageUrl", "/test/image.jpg")
                .sample();

        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.moveParent(anyLong(), anyLong())).willReturn(result);
        assert result != null;
        CategoryResponse.Detail response = CategoryResponse.Detail.from(result);
        //when
        //then
        mockMvc.perform(post("/categories/{categoryId}/move", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("01-category-06-move",
                                "카테고리 부모 변경",
                                "카테고리의 부모를 변경",
                                CategoryDescriptor.getMoveCategoryRequest(),
                                CategoryDescriptor.getCategoryResponse(),
                                parameterWithName("categoryId").description("수정할 카테고리 ID"))
                );
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    void deleteCategory() throws Exception {
        //given
        HttpHeaders adminHeader = createAdminHeader();
        willDoNothing().given(categoryService).deleteCategory(anyLong());
        //when
        //then
        mockMvc.perform(delete("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(createSecuredDocument("01-category-07-delete",
                        "카테고리 삭제",
                        "카테고리를 삭제한다")
                );
    }

    private CategoryNavigationResult createNavigation() {
        CategoryResult electron = createCategoryResponse().id(1L).name("전자기기").parentId(null).depth(1).imagePath("/test/electron.jpg").build();
        CategoryResult laptop = createCategoryResponse().id(2L).name("노트북").parentId(1L).depth(2).imagePath("/test/laptop.jpg").build();
        CategoryResult desktop = createCategoryResponse().id(3L).name("데스크탑").parentId(1L).depth(2).imagePath("/test/desktop.jpg").build();
        CategoryResult light = createCategoryResponse().id(4L).name("경량 노트북").parentId(2L).depth(3).imagePath("/test/lightLaptop.jpg").build();
        CategoryResult gaming = createCategoryResponse().id(5L).name("게이밍 노트북").parentId(2L).depth(3).imagePath("/test/gamingLaptop.jpg").build();

        return  CategoryNavigationResult.builder()
                .current(laptop)
                .path(List.of(electron, laptop))
                .siblings(List.of(desktop))
                .children(List.of(light, gaming))
                .build();
    }

    private List<CategoryTreeResult> mappingTreeResponse() {
        CategoryTreeResult electron = createCategoryTreeResponse(1L, "전자기기", null, 1, "/test/electron.jpg");
        CategoryTreeResult laptop = createCategoryTreeResponse(3L, "노트북", 1L, 2, "/test/laptop.jpg");
        CategoryTreeResult cellPhone = createCategoryTreeResponse(4L, "핸드폰", 1L, 2, "/test/cellPhone.jpg");
        electron.addChild(laptop);
        electron.addChild(cellPhone);

        CategoryTreeResult food = createCategoryTreeResponse(2L, "식품", null, 1, "/test/food.jpg");
        CategoryTreeResult meat = createCategoryTreeResponse(5L, "육류", 2L, 2, "/test/meat.jpg");
        CategoryTreeResult vegetable = createCategoryTreeResponse(6L, "채소류", 2L, 2, "/test/vegetable.jpg");
        food.addChild(meat);
        food.addChild(vegetable);
        return List.of(electron, food);
    }

    private CategoryTreeResult createCategoryTreeResponse(Long id, String name, Long parentId, int depth,
                                                          String imagePath) {

        return CategoryTreeResult.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .depth(depth)
                .imagePath(imagePath)
                .build();
    }


    private CategoryResult.CategoryResultBuilder createCategoryResponse() {
        return CategoryResult.builder()
                .id(1L)
                .name("카테고리")
                .parentId(null)
                .depth(1)
                .imagePath("/test/category.jpg");
    }
}
