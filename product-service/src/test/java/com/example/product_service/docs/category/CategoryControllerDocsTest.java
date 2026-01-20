package com.example.product_service.docs.category;

import com.example.product_service.api.category.controller.CategoryController;
import com.example.product_service.api.category.controller.dto.CategoryRequest;
import com.example.product_service.api.category.controller.dto.MoveCategoryRequest;
import com.example.product_service.api.category.controller.dto.UpdateCategoryRequest;
import com.example.product_service.api.category.service.CategoryService;
import com.example.product_service.api.category.service.dto.result.CategoryNavigationResponse;
import com.example.product_service.api.category.service.dto.result.CategoryResponse;
import com.example.product_service.api.category.service.dto.result.CategoryTreeResponse;
import com.example.product_service.docs.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryControllerDocsTest extends RestDocsSupport {

    CategoryService categoryService = mock(CategoryService.class);

    @Override
    protected Object initController() {
        return new CategoryController(categoryService);
    }

    @Test
    @DisplayName("카테고리를 저장한다")
    void saveCategory() throws Exception {
        //given
        CategoryRequest request = createCategoryRequest().build();
        CategoryResponse response = createCategoryResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.saveCategory(anyString(), nullable(Long.class), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/categories")
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
                                        fieldWithPath("depth").description("카테고리 깊이"),
                                        fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
                                )
                        )
                );
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
                                        subsectionWithPath("path").description("직계 카테고리"),
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
        CategoryResponse response = createCategoryResponse().build();
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

    @Test
    @DisplayName("카테고리를 수정한다")
    void updateCategory() throws Exception {
        //given
        UpdateCategoryRequest request = createUpdateCategoryRequest().build();
        CategoryResponse response = createCategoryResponse().name("새 카테고리").imageUrl("http://newCategory.jpg").build();
        HttpHeaders adminHeader = createAdminHeader();
        given(categoryService.updateCategory(anyLong(), anyString(), anyString()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/categories/{categoryId}", 1L)
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
                                pathParameters(
                                        parameterWithName("categoryId").description("수정할 카테고리 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("변경할 카테고리 이름"),
                                        fieldWithPath("imageUrl").description("변경할 카테고리 아이콘 URL")
                                ),
                                responseFields(
                                        fieldWithPath("id").description("카테고리 ID"),
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("parentId").description("부모 카테고리 ID").type("Number"),
                                        fieldWithPath("depth").description("카테고리 깊이"),
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
        mockMvc.perform(post("/categories/{categoryId}/move", 1L)
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
                                pathParameters(
                                        parameterWithName("categoryId").description("수정할 카테고리 ID")
                                ),
                                requestFields(
                                        fieldWithPath("parentId").description("이동할 부모 카테고리 ID")

                                ),
                                responseFields(
                                        fieldWithPath("id").description("카테고리 ID"),
                                        fieldWithPath("name").description("카테고리 이름"),
                                        fieldWithPath("parentId").description("부모 카테고리 ID").type("Number"),
                                        fieldWithPath("depth").description("카테고리 깊이"),
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
        willDoNothing().given(categoryService).deleteCategory(anyLong());
        //when
        //then
        mockMvc.perform(delete("/categories/{categoryId}", 1L)
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
                                ),
                                pathParameters(
                                        parameterWithName("categoryId").description("삭제할 카테고리 ID")
                                )
                        )
                );
    }

    private CategoryNavigationResponse createNavigation() {
        CategoryResponse electron = createCategoryResponse().id(1L).name("전자기기").parentId(null).depth(1).imageUrl("http://electron.jpg").build();
        CategoryResponse laptop = createCategoryResponse().id(2L).name("노트북").parentId(1L).depth(2).imageUrl("http://laptop.jpg").build();
        CategoryResponse desktop = createCategoryResponse().id(3L).name("데스크탑").parentId(1L).depth(2).imageUrl("http://desktop.jpg").build();
        CategoryResponse light = createCategoryResponse().id(4L).name("경량 노트북").parentId(2L).depth(3).imageUrl("http://lightlaptop.jpg").build();
        CategoryResponse gaming = createCategoryResponse().id(5L).name("게이밍 노트북").parentId(2L).depth(3).imageUrl("http://gaminglaptop.jpg").build();

        return  CategoryNavigationResponse.builder()
                .current(laptop)
                .path(List.of(electron, laptop))
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

    private static UpdateCategoryRequest.UpdateCategoryRequestBuilder createUpdateCategoryRequest() {
        return UpdateCategoryRequest.builder()
                .name("새 카테고리")
                .imageUrl("http://newCategory.jpg");
    }

    private CategoryResponse.CategoryResponseBuilder createCategoryResponse() {
        return CategoryResponse.builder()
                .id(1L)
                .name("카테고리")
                .parentId(null)
                .depth(1)
                .imageUrl("http://category.jpg");
    }

    private static CategoryRequest.CategoryRequestBuilder createCategoryRequest() {
        return CategoryRequest.builder()
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

    private MoveCategoryRequest createMoveCategoryRequest() {
        return MoveCategoryRequest.builder()
                .parentId(2L)
                .build();
    }
}
