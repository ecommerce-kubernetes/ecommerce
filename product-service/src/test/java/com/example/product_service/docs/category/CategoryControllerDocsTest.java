package com.example.product_service.docs.category;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
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
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryControllerDocsTest extends RestDocsSupport {

    CategoryService categoryService = mock(CategoryService.class);

    private static final String TAG = "Category";
    
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

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("name").description("카테고리 이름"),
                fieldWithPath("parentId").description("부모 카테고리 ID").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("카테고리 ID"),
                fieldWithPath("name").description("카테고리 이름"),
                fieldWithPath("parentId").description("부모 카테고리 ID").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("depth").description("카테고리 깊이"),
                fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
        };
        //when
        //then
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(
                        document("01-category-01-create",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("카테고리 생성")
                                                .description("새로운 카테고리를 생성합니다")
                                                .requestHeaders(requestHeaders)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                requestFields(requestFields),
                                responseFields(responseFields)
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

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("[].id").description("카테고리 ID"),
                fieldWithPath("[].name").description("카테고리 이름"),
                fieldWithPath("[].parentId").description("부모 카테고리 ID").optional(),
                fieldWithPath("[].depth").description("카테고리 깊이"),
                fieldWithPath("[].imageUrl").description("카테고리 이미지 URL"),
                subsectionWithPath("[].children").description("하위 카테고리 목록 (상위 구조와 동일)")
        };
        //when
        //then
        mockMvc.perform(get("/categories/tree"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("01-category-02-get-tree",
                                preprocessResponse(prettyPrint()),
                                resource(
                                    ResourceSnippetParameters.builder()
                                            .tag(TAG)
                                            .summary("카테고리 트리 조회")
                                            .description("전체 카테고리를 트리 구조로 조회합니다")
                                            .responseFields(responseFields)
                                            .build()
                                ),
                                responseFields(responseFields)
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

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("categoryId").description("조회할 카테고리 ID")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                subsectionWithPath("current").description("요청 카테고리"),
                subsectionWithPath("path").description("직계 카테고리"),
                subsectionWithPath("siblings").description("형제 카테고리"),
                subsectionWithPath("children").description("자식 카테고리")
        };

        //when
        //then
        mockMvc.perform(get("/categories/navigation/{categoryId}", 2L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("01-category-03-get-navigation",
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("카테고리 네비게이션 조회")
                                                .description("특정 카테고리의 네비게이션 구조를 조회합니다")
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                pathParameters(pathParameters),
                                responseFields(responseFields)

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

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("categoryId").description("조회할 카테고리 ID")
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("카테고리 ID"),
                fieldWithPath("name").description("카테고리 이름"),
                fieldWithPath("parentId").description("부모 카테고리 ID").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("depth").description("카테고리 깊이"),
                fieldWithPath("imageUrl").description("카테고리 이미지 URL")
        };
        //when
        //then
        mockMvc.perform(get("/categories/{categoryId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "01-category-04-get",
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("카테고리 단건 조회")
                                                .description("ID로 특정 카테고리의 상세 정보 조회")
                                                .pathParameters(pathParameters)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                pathParameters(pathParameters),
                                responseFields(responseFields)
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

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("categoryId").description("수정할 카테고리 ID")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("name").description("변경할 카테고리 이름").optional(),
                fieldWithPath("imageUrl").description("변경할 카테고리 아이콘 URL").optional()
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("카테고리 ID"),
                fieldWithPath("name").description("카테고리 이름"),
                fieldWithPath("parentId").description("부모 카테고리 ID").type("Number").optional(),
                fieldWithPath("depth").description("카테고리 깊이"),
                fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
        };

        //when
        //then
        mockMvc.perform(patch("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("01-category-05-update",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("카테고리 수정")
                                                .description("ID로 특정 카테고리의 기본 정보 수정")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters),
                                requestFields(requestFields),
                                responseFields(responseFields)
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

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("categoryId").description("수정할 카테고리 ID")
        };

        FieldDescriptor[] requestFields = new FieldDescriptor[] {
                fieldWithPath("parentId").description("이동할 부모 카테고리 ID").optional()
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
                fieldWithPath("id").description("카테고리 ID"),
                fieldWithPath("name").description("카테고리 이름"),
                fieldWithPath("parentId").description("부모 카테고리 ID").type("Number"),
                fieldWithPath("depth").description("카테고리 깊이"),
                fieldWithPath("imageUrl").description("카테고리 아이콘 URL")
        };

        //when
        //then
        mockMvc.perform(post("/categories/{categoryId}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("01-category-06-move",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("카테고리 부모 변경")
                                                .description("카테고리의 부모를 변경")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .requestFields(requestFields)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters),
                                requestFields(requestFields),
                                responseFields(responseFields)
                        )
                );
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    void deleteCategory() throws Exception {
        //given
        HttpHeaders adminHeader = createAdminHeader();
        willDoNothing().given(categoryService).deleteCategory(anyLong());

        HeaderDescriptor[] requestHeaders = new HeaderDescriptor[] {
                headerWithName("Authorization").description("JWT Access Token")
        };

        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("categoryId").description("삭제할 카테고리 ID")
        };
        
        //when
        //then
        mockMvc.perform(delete("/categories/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("01-category-07-delete",
                                preprocessRequest(prettyPrint(),
                                        modifyHeaders()
                                                .remove("X-User-Id")
                                                .remove("X-User-Role")
                                                .add("Authorization", "Bearer {ACCESS_TOKEN}")),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("카테고리 삭제")
                                                .description("카테고리를 삭제한다")
                                                .requestHeaders(requestHeaders)
                                                .pathParameters(pathParameters)
                                                .build()
                                ),
                                requestHeaders(requestHeaders),
                                pathParameters(pathParameters)
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
