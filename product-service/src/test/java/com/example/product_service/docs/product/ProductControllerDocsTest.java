package com.example.product_service.docs.product;

import com.example.product_service.api.product.controller.ProductController;
import com.example.product_service.api.product.controller.dto.*;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.AddVariantCommand;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.dto.response.PageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.example.product_service.support.fixture.ProductControllerFixture.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerDocsTest extends RestDocsSupport {
    ProductService productService = Mockito.mock(ProductService.class);
    @Override
    protected Object initController() {
        return new ProductController(productService);
    }

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() throws Exception {
        //given
        ProductCreateRequest request = mockCreateRequest().build();
        ProductCreateResponse response = mockCreateResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.createProduct(any(ProductCreateCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        document("create-product",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                requestFields(
                                        fieldWithPath("name").description("상품 이름").optional(),
                                        fieldWithPath("categoryId").description("카테고리 Id").optional(),
                                        fieldWithPath("description").description("상품 설명"),
                                        fieldWithPath("price").description("임시 가격").optional()
                                ),

                                responseFields(
                                        fieldWithPath("productId").description("상품 Id")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품 옵션 정의")
    void addOptionSpec() throws Exception {
        //given
        ProductOptionSpecRequest request = mockOptionSpecRequest().build();
        ProductOptionSpecResponse response = mockOptionSpecResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.addOptionSpec(anyLong(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products/{productId}/option-specs", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("add-option-specs",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                pathParameters(
                                        parameterWithName("productId").description("옵션을 추가할 상품 ID")
                                ),

                                requestFields(
                                        fieldWithPath("optionTypeIds").description("옵션 타입 Id 리스트")
                                ),

                                responseFields(
                                        fieldWithPath("productId").description("상품 Id"),
                                        fieldWithPath("options[].productOptionId").description("상품 옵션 Id"),
                                        fieldWithPath("options[].optionTypeId").description("옵션 타입 Id"),
                                        fieldWithPath("options[].name").description("옵션 타입 이름"),
                                        fieldWithPath("options[].priority").description("상품 옵션 순서")
                                )

                        )
                );
    }

    @Test
    @DisplayName("상품 변형 추가")
    void addVariant() throws Exception {
        //given
        VariantCreateRequest request = mockCreateVariantRequest().build();
        VariantCreateResponse response = mockCreateVariantResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.addVariants(any(AddVariantCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products/{productId}/variants", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("add-variants",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                pathParameters(
                                        parameterWithName("productId").description("상품 변형을 추가할 상품 ID")
                                ),

                                requestFields(
                                        fieldWithPath("variants[].price").description("상품 변형 가격"),
                                        fieldWithPath("variants[].discountRate").description("할인율"),
                                        fieldWithPath("variants[].stockQuantity").description("재고 수량"),
                                        fieldWithPath("variants[].optionValueIds").description("옵션 값 Id 리스트")
                                ),

                                responseFields(
                                        fieldWithPath("productId").description("상품 Id"),
                                        fieldWithPath("variants[].variantId").description("상품 변형 Id"),
                                        fieldWithPath("variants[].sku").description("상품 SKU"),
                                        fieldWithPath("variants[].optionValueIds").description("상품 변형 옵션 값 ID 리스트"),
                                        fieldWithPath("variants[].originalPrice").description("상품 변형 원본 가격"),
                                        fieldWithPath("variants[].discountedPrice").description("상품 변형 할인 가격"),
                                        fieldWithPath("variants[].discountRate").description("상품 변형 할인율"),
                                        fieldWithPath("variants[].stockQuantity").description("상품 변형 재고 수량")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품 이미지 추가")
    void addImages() throws Exception {
        //given
        ProductImageCreateRequest request = mockImageRequest().build();
        ProductImageCreateResponse response = mockImageResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.addImages(anyLong(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(put("/products/{productId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("add-images",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                pathParameters(
                                        parameterWithName("productId").description("상품 변형을 추가할 상품 ID")
                                ),

                                requestFields(
                                        fieldWithPath("images").description("상품 이미지 URL 리스트")
                                ),

                                responseFields(
                                        fieldWithPath("productId").description("상품 Id"),
                                        fieldWithPath("images[].productImageId").description("상품 이미지 Id"),
                                        fieldWithPath("images[].imageUrl").description("상품 이미지 URL"),
                                        fieldWithPath("images[].order").description("상품 이미지 순서"),
                                        fieldWithPath("images[].thumbnail").description("썸네일 여부")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품을 게시한다")
    void publishProduct() throws Exception {
        //given
        ProductPublishResponse response = mockPublishResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.publish(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/publish", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("add-images",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                pathParameters(
                                        parameterWithName("productId").description("상품 변형을 추가할 상품 ID")
                                ),

                                responseFields(
                                        fieldWithPath("productId").description("상품 Id"),
                                        fieldWithPath("status").description("상품 상태"),
                                        fieldWithPath("publishedAt").description("게시일")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품 목록을 조회한다")
    void getProducts() throws Exception {
        //given
        ProductSummaryResponse summary = mockSummaryResponse().build();
        PageDto<ProductSummaryResponse> response = PageDto.<ProductSummaryResponse>builder().content(List.of(summary))
                .currentPage(1)
                .totalPage(10)
                .pageSize(10)
                .totalElement(100)
                .build();
        given(productService.getProducts(any(ProductSearchCondition.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "latest")
                        .param("categoryId", "1")
                .param("name", "나이키")
                .param("rating", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("get-products",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                queryParameters(
                                        parameterWithName("page").description("페이지 번호 (기본값: 1)"),
                                        parameterWithName("size").description("페이지 크기 (기본값: 20, 최대: 100)"),
                                        parameterWithName("sort").description("정렬 기준 (latest: 최신순 등)"),
                                        parameterWithName("categoryId").description("카테고리 Id"),
                                        parameterWithName("name").description("상품명 검색 키워드"),
                                        parameterWithName("rating").description("상품 평점")
                                ),

                                responseFields(
                                        fieldWithPath("content[].productId").description("상품 번호"),
                                        fieldWithPath("content[].name").description("상품 이름"),
                                        fieldWithPath("content[].thumbnail").description("썸네일"),
                                        fieldWithPath("content[].displayPrice").description("대표 상품 판매 가격"),

                                        fieldWithPath("content[].originalPrice").description("대표 상품 원본 가격"),
                                        fieldWithPath("content[].maxDiscountRate").description("최대 할인율"),
                                        fieldWithPath("content[].categoryId").description("카테고리 Id"),
                                        fieldWithPath("content[].publishedAt").description("게시일"),
                                        fieldWithPath("content[].rating").description("평점"),
                                        fieldWithPath("content[].reviewCount").description("리뷰 개수"),
                                        fieldWithPath("content[].status").description("상품 상태"),

                                        fieldWithPath("currentPage").description("현재 페이지"),
                                        fieldWithPath("totalPage").description("총 페이지"),
                                        fieldWithPath("pageSize").description("페이지 크기"),
                                        fieldWithPath("totalElement").description("총 데이터 양")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품을 조회한다")
    void getProduct() throws Exception {
        //given
        ProductDetailResponse response = mockDetailResponse().build();
        given(productService.getProduct(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(get("/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("get-product",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("productId").description("조회할 상품 ID")
                                ),
                                responseFields(
                                    fieldWithPath("productId").description("상품 ID"),
                                    fieldWithPath("name").description("상품 이름"),
                                    fieldWithPath("status").description("상품 상태"),
                                    fieldWithPath("categoryId").description("카테고리 Id"),
                                    fieldWithPath("description").description("상품 설명"),
                                    fieldWithPath("displayPrice").description("대표 상품 판매 가격"),
                                    fieldWithPath("originalPrice").description("대표 상품 원본 가격"),
                                    fieldWithPath("maxDiscountRate").description("최대 할인율"),
                                    fieldWithPath("rating").description("평점"),
                                    fieldWithPath("reviewCount").description("리뷰 갯수"),
                                    fieldWithPath("optionGroups[].optionTypeId").description("상품 옵션 타입 Id"),
                                    fieldWithPath("optionGroups[].name").description("상품 옵션 타입 이름"),
                                    fieldWithPath("optionGroups[].values[].optionValueId").description("상품 옵션 값 ID"),
                                    fieldWithPath("optionGroups[].values[].name").description("상품 옵션 값 이름"),
                                    fieldWithPath("images[].productImageId").description("상품 이미지 Id"),
                                    fieldWithPath("images[].imageUrl").description("상품 이미지 URL"),
                                    fieldWithPath("images[].order").description("상품 이미지 순서"),
                                    fieldWithPath("images[].thumbnail").description("썸네일 여부"),
                                    fieldWithPath("variants[].variantId").description("상품 변형 ID"),
                                    fieldWithPath("variants[].sku").description("상품 변형 SKU"),
                                    fieldWithPath("variants[].optionValueIds").description("상품 변형 옵션 값 Id 리스트"),
                                    fieldWithPath("variants[].originalPrice").description("상품 변형 원본 가격"),
                                    fieldWithPath("variants[].discountedPrice").description("상품 변형 할인 가격"),
                                    fieldWithPath("variants[].discountRate").description("상품 변형 할인율"),
                                    fieldWithPath("variants[].stockQuantity").description("상품 변형 재고 수량")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품 정보를 수정한다")
    void updateProduct() throws Exception {
        //given
        ProductUpdateRequest request = mockUpdateRequest().build();
        ProductUpdateResponse response = mockUpdateResponse().build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.updateProduct(any(ProductUpdateCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(put("/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("update-product",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                pathParameters(
                                        parameterWithName("productId").description("수정할 상품 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("변경할 상품 이름"),
                                        fieldWithPath("categoryId").description("변경할 카테고리 Id"),
                                        fieldWithPath("description").description("변경할 상품 설명")
                                ),
                                responseFields(
                                        fieldWithPath("productId").description("상품 ID"),
                                        fieldWithPath("name").description("상품 이름"),
                                        fieldWithPath("categoryId").description("카테고리 Id"),
                                        fieldWithPath("description").description("상품 설명")
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품을 삭제한다")
    void deleteProduct() throws Exception {
        //given
        willDoNothing().given(productService).deleteProduct(anyLong());
        HttpHeaders adminHeader = createAdminHeader();
        //when
        //then
        mockMvc.perform(delete("/products/{productId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(
                        document("delete-product",
                                preprocessResponse(prettyPrint()),

                                requestHeaders(
                                        headerWithName("X-User-Id").description(USER_ID_HEADER_DESCRIPTION).optional(),
                                        headerWithName("X-User-Role").description(USER_ROLE_HEADER_DESCRIPTION).optional()
                                ),

                                pathParameters(
                                        parameterWithName("productId").description("수정할 상품 ID")
                                )
                        )
                );
    }

    private HttpHeaders createAdminHeader(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", "1");
        headers.add("X-User-Role", "ROLE_ADMIN");
        return headers;
    }
}
