package com.example.product_service.docs.category;

import com.example.product_service.category.adapter.in.web.CategoryController;
import com.example.product_service.category.adapter.in.web.dto.request.CreateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.MoveCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.request.UpdateCategoryRequest;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryDetailResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryListResponse;
import com.example.product_service.category.adapter.in.web.dto.response.CategoryTreeListResponse;
import com.example.product_service.category.application.service.CategoryService;
import com.example.product_service.category.application.service.dto.command.CategoryCommand;
import com.example.product_service.category.application.service.dto.result.CategoryResult;
import com.example.product_service.category.fixture.CategoryRequestFixture;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.example.product_service.docs.descriptor.CategoryDescriptor.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerDocsTest extends RestDocsSupport {

    CategoryService categoryService = Mockito.mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new CategoryController(categoryService);
    }

    @Test
    @DisplayName("카테고리를 저장한다")
    void saveCategory() throws Exception {
        //given
        CreateCategoryRequest request = CategoryRequestFixture.anCreateCategoryRequest().build();
        Long categoryId = 1L;

        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");

        given(categoryService.saveCategory(any(CategoryCommand.Create.class)))
                .willReturn(categoryId);
        //when
        //then
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "admin/categories",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(createRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(createResponse())
                ));
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    void updateCategory() throws Exception {
        //given
        UpdateCategoryRequest request = CategoryRequestFixture.anUpdateCategoryRequest().build();

        Long categoryId = 1L;
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(categoryService.updateCategory(any(CategoryCommand.Update.class)))
                .willReturn(categoryId);
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/categories/update",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(updateRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(updateResponse())
                ));
    }

    @Test
    @DisplayName("카테고리의 부모를 변경한다")
    void moveParent() throws Exception {
        //given
        MoveCategoryRequest request = CategoryRequestFixture.anMoveCategoryRequest().build();

        Long categoryId = 1L;

        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(categoryService.moveParent(anyLong(), anyLong())).willReturn(categoryId);
        //when
        //then
        mockMvc.perform(patch("/admin/categories/{categoryId}/move", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "admin/categories/move",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(moveRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(moveResponse())
                ));
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    void deleteCategory() throws Exception {
        //given
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        willDoNothing().given(categoryService).deleteCategory(anyLong());
        //when
        //then
        mockMvc.perform(delete("/admin/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "admin/categories/delete",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER)
                ));
    }

    @Test
    @DisplayName("최상위 카테고리 목록을 조회한다")
    void getRootCategories() throws Exception {
        //given
        List<CategoryResult.Tree> results = mappingTreeResponse();
        given(categoryService.getTree()).willReturn(results);
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
        List<CategoryResult.Tree> results = mappingTreeResponse();
        CategoryResult.Tree electron = results.get(0);
        given(categoryService.getTree()).willReturn(results);
        CategoryListResponse response = CategoryListResponse.fromChildren(electron);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}/children", electron.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document(
                        "categories/children",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(childResponse())
                ));
    }

    @Test
    @DisplayName("카테고리를 조회한다")
    void getCategory() throws Exception {
        //given
        CategoryResult.Navigation result = createNavigation();
        given(categoryService.getNavigation(anyLong()))
                .willReturn(result);
        CategoryDetailResponse response = CategoryDetailResponse.from(result);
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 2L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
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
        List<CategoryResult.Tree> results = mappingTreeResponse();
        given(categoryService.getTree()).willReturn(results);
        CategoryTreeListResponse response = CategoryTreeListResponse.from(results);
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(document(
                        "categories/tree",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(treeResponse())
                ));
    }

    private CategoryResult.Navigation createNavigation() {
        CategoryResult.Detail electron = createCategoryResponse().id(1L).name("전자기기").parentId(null).depth(1).imagePath("/test/electron.jpg").build();
        CategoryResult.Detail laptop = createCategoryResponse().id(2L).name("노트북").parentId(1L).depth(2).imagePath("/test/laptop.jpg").build();
        CategoryResult.Detail desktop = createCategoryResponse().id(3L).name("데스크탑").parentId(1L).depth(2).imagePath("/test/desktop.jpg").build();
        CategoryResult.Detail light = createCategoryResponse().id(4L).name("경량 노트북").parentId(2L).depth(3).imagePath("/test/lightLaptop.jpg").build();
        CategoryResult.Detail gaming = createCategoryResponse().id(5L).name("게이밍 노트북").parentId(2L).depth(3).imagePath("/test/gamingLaptop.jpg").build();

        return CategoryResult.Navigation.builder()
                .current(laptop)
                .path(List.of(electron, laptop))
                .siblings(List.of(desktop))
                .children(List.of(light, gaming))
                .build();
    }

    private List<CategoryResult.Tree> mappingTreeResponse() {
        CategoryResult.Tree electron = createCategoryTreeResponse(1L, "전자기기", null, 1, "/test/electron.jpg");
        CategoryResult.Tree laptop = createCategoryTreeResponse(3L, "노트북", 1L, 2, "/test/laptop.jpg");
        CategoryResult.Tree cellPhone = createCategoryTreeResponse(4L, "핸드폰", 1L, 2, "/test/cellPhone.jpg");
        electron.addChild(laptop);
        electron.addChild(cellPhone);

        CategoryResult.Tree food = createCategoryTreeResponse(2L, "식품", null, 1, "/test/food.jpg");
        CategoryResult.Tree meat = createCategoryTreeResponse(5L, "육류", 2L, 2, "/test/meat.jpg");
        CategoryResult.Tree vegetable = createCategoryTreeResponse(6L, "채소류", 2L, 2, "/test/vegetable.jpg");
        food.addChild(meat);
        food.addChild(vegetable);
        return List.of(electron, food);
    }

    private CategoryResult.Tree createCategoryTreeResponse(Long id, String name, Long parentId, int depth,
                                                           String imagePath) {
        return CategoryResult.Tree.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .depth(depth)
                .imagePath(imagePath)
                .build();
    }

    private CategoryResult.Detail.DetailBuilder createCategoryResponse() {
        return CategoryResult.Detail.builder()
                .id(1L)
                .name("카테고리")
                .parentId(null)
                .depth(1)
                .imagePath("/test/category.jpg");
    }
}
