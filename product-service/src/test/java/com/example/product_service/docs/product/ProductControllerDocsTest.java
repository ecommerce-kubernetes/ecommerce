package com.example.product_service.docs.product;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.product_service.api.common.dto.PageDto;
import com.example.product_service.api.product.controller.ProductController;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.controller.dto.request.ProductRequest.*;
import com.example.product_service.api.product.controller.dto.response.ProductResponse.*;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.docs.descriptor.ProductDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.example.product_service.support.fixture.ProductControllerFixture.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerDocsTest extends RestDocsSupport {
    ProductService productService = Mockito.mock(ProductService.class);

    @Override
    protected String getTag() {
        return "Product";
    }

    @Override
    protected Object initController() {
        return new ProductController(productService);
    }

    private static final String TAG = "Product";

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() throws Exception {
        //given
        CreateRequest request = fixtureMonkey.giveMeBuilder(CreateRequest.class)
                .set("name", "상품")
                .set("categoryId", 1L)
                .set("description", "상품 설명")
                .sample();
        ProductCreateResult result = fixtureMonkey.giveMeBuilder(ProductCreateResult.class)
                .set("productId", 1L)
                .sample();
        assert result != null;
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.createProduct(any(ProductCreateCommand.class)))
                .willReturn(result);
        CreateResponse response = CreateResponse.from(result);
        //when
        //then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(print())
                .andDo(createSecuredDocument("03-product-01-create",
                                "상품 생성",
                                "새로운 상품을 생성합니다",
                                ProductDescriptor.getCreateRequest(),
                                ProductDescriptor.getCreateResponse())
                );
    }

    @Test
    @DisplayName("상품 옵션 정의")
    void registerProductOption() throws Exception {
        //given
        OptionRegisterRequest request = fixtureMonkey.giveMeBuilder(OptionRegisterRequest.class)
                .set("options", List.of(
                        ProductOptionRequest.builder()
                                .optionTypeId(1L)
                                .priority(1)
                                .build()
                ))
                .sample();
        ProductOptionResponse result = fixtureMonkey.giveMeBuilder(ProductOptionResponse.class)
                .set("productId", 1L)
                .size("options", 1)
                .set("options[0].optionTypeId", 1L)
                .set("options[0].optionTypeName", "사이즈")
                .set("options[0].priority", 1)
                .sample();
        assert result != null;
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.defineOptions(anyLong(), anyList()))
                .willReturn(result);
        OptionRegisterResponse response = OptionRegisterResponse.from(result);
        //when
        //then
        mockMvc.perform(put("/products/{productId}/options", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("03-product-02-add-option",
                                "상품 옵션 추가",
                                "상품에 상품 옵션을 추가",
                                ProductDescriptor.getRegisterOptionRequest(),
                                ProductDescriptor.getRegisterOptionResponse(),
                                parameterWithName("productId").description("옵션을 추가할 상품 ID"))
                );
    }

    @Test
    @DisplayName("상품 변형 추가")
    void addVariants() throws Exception {
        //given
        AddVariantRequest request = fixtureMonkey.giveMeBuilder(AddVariantRequest.class)
                .size("variants", 1)
                .set("variants[0].originalPrice", 10000L)
                .set("variants[0].discountRate", 10)
                .set("variants[0].stockQuantity", 100)
                .set("variants[0].optionValueIds", List.of(1L, 2L))
                .sample();
        AddVariantResult result = fixtureMonkey.giveMeBuilder(AddVariantResult.class)
                .size("variants", 1)
                .set("productId", 1L)
                .set("variants[0].variantId", 1L)
                .set("variants[0].sku", "PROD_XL_BLUE")
                .set("variants[0].optionValueIds", List.of(1L, 2L))
                .set("variants[0].originalPrice", 10000L)
                .set("variants[0].discountedPrice", 9000L)
                .set("variants[0].discountRate", 10)
                .set("variants[0].stockQuantity", 100)
                .sample();
        assert result != null;
        AddVariantResponse response = AddVariantResponse.from(result);
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.createVariants(any(ProductVariantsCreateCommand.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/products/{productId}/variants", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument(
                        "03-product-03-add-variants",
                        "상품 변형 추가",
                        "상품에 상품 변형을 추가",
                        ProductDescriptor.getAddVariantRequest(),
                        ProductDescriptor.getAddVariantResponse(),
                        parameterWithName("productId").description("상품 변형을 추가할 상품 ID"))
                );
    }

    @Test
    @DisplayName("상품 이미지 추가")
    void updateImages() throws Exception {
        //given
        AddImageRequest request = fixtureMonkey.giveMeBuilder(AddImageRequest.class)
                .size("images", 1)
                .set("images[0].imagePath", "/test/image.jpg")
                .set("images[0].isThumbnail", true)
                .set("images[0].sortOrder", 1)
                .sample();
        ProductImageCreateResult result = fixtureMonkey.giveMeBuilder(ProductImageCreateResult.class)
                .size("images", 1)
                .set("productId", 1L)
                .set("images[0].imageId", 1L)
                .set("images[0].imagePath", "/test/image.jpg")
                .set("images[0].isThumbnail", true)
                .set("images[0].sortOrder", 1)
                .sample();
        assert result != null;
        AddImageResponse response = AddImageResponse.from(result);
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.updateImages(anyLong(), anyList()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(put("/products/{productId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("03-product-04-add-images",
                        "상품 이미지 추가",
                        "상품 이미지를 추가",
                        ProductDescriptor.getAddImageRequest(),
                        ProductDescriptor.getAddImageResponse(),
                        parameterWithName("productId").description("이미지를 추가할 상품 ID"))
                );
    }

    @Test
    @DisplayName("상품 설명 이미지 추가")
    void updateDescriptionImage() throws Exception {
        AddDescriptionImageRequest request = fixtureMonkey.giveMeBuilder(AddDescriptionImageRequest.class)
                .size("images", 1)
                .set("images[0].imagePath", "/test/image.jpg")
                .set("images[0].sortOrder", 1)
                .sample();
        ProductDescriptionImageResult result = fixtureMonkey.giveMeBuilder(ProductDescriptionImageResult.class)
                .size("descriptionImages", 1)
                .set("productId", 1L)
                .set("descriptionImages[0].imageId", 1L)
                .set("descriptionImages[0].imagePath", "/test/image.jpg")
                .set("descriptionImages[0].sortOrder", 1)
                .sample();
        assert result != null;
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.updateDescriptionImages(anyLong(), anyList()))
                .willReturn(result);
        AddDescriptionImageResponse response = AddDescriptionImageResponse.from(result);
        mockMvc.perform(put("/products/{productId}/description-images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("03-product-05-add-description-images",
                                "상품 설명 이미지 추가",
                                "상품 설명 이미지를 추가",
                                ProductDescriptor.getAddDescriptionImageRequest(),
                                ProductDescriptor.getAddDescriptionImageResponse(),
                                parameterWithName("productId").description("설명 이미지를 추가할 상품 ID"))
                );

    }

    @Test
    @DisplayName("상품을 게시한다")
    void publishProduct() throws Exception {
        //given
        ProductStatusResult result = fixtureMonkey.giveMeBuilder(ProductStatusResult.class)
                .set("productId", 1L)
                .set("status", "ON_SALE")
                .set("publishedAt", LocalDateTime.now())
                .sample();
        assert result != null;
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.publish(anyLong()))
                .willReturn(result);
        PublishResponse response = PublishResponse.from(result);
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/publish", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("03-product-06-publish",
                        "상품 판매 개시",
                        "상품을 판매 개시한다",
                        ProductDescriptor.getPublishResponse(),
                        parameterWithName("productId").description("게시할 상품 상품 ID"))
                );
    }

    @Test
    @DisplayName("상품을 판매 중지로 변경한다")
    void closeProduct() throws Exception {
        //given
        ProductStatusResult result = fixtureMonkey.giveMeBuilder(ProductStatusResult.class)
                .set("productId", 1L)
                .set("status", "STOP_SALE")
                .set("saleStoppedAt", LocalDateTime.now())
                .sample();
        assert result != null;
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.closedProduct(anyLong()))
                .willReturn(result);
        CloseResponse response = CloseResponse.from(result);
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/close", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createSecuredDocument("03-product-11-close",
                                "상품 판매 중지",
                                "상품을 판매 중지한다",
                                ProductDescriptor.getCloseResponse(),
                                parameterWithName("productId").description("판매 중지할 상품 ID"))
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
        ParameterDescriptor[] queryParameters = new ParameterDescriptor[] {
                parameterWithName("page").description("페이지 번호 (기본값: 1)").optional(),
                parameterWithName("size").description("페이지 크기 (기본값: 20, 최대: 100)").optional(),
                parameterWithName("sort").description("정렬 기준 (latest: 최신순 등)").optional(),
                parameterWithName("categoryId").description("카테고리 ID").optional(),
                parameterWithName("name").description("상품명 검색 키워드").optional(),
                parameterWithName("rating").description("상품 평점 (이 점수 이상)").optional()
        };

        FieldDescriptor[] responseFields = new FieldDescriptor[] {
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
        };
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
                        document("03-product-07-get-products",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("상품 목록 조회")
                                                .description("상품 목록을 조회한다")
                                                .queryParameters(queryParameters)
                                                .responseFields(responseFields)
                                                .build()
                                ),
                                queryParameters(queryParameters),
                                responseFields(responseFields)
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
        ParameterDescriptor[] pathParameters = new ParameterDescriptor[] {
                parameterWithName("productId").description("조회할 상품 ID")
        };
        FieldDescriptor[] responseFields = new FieldDescriptor[] {
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
                fieldWithPath("popularityScore").description("인기 점수"),
                fieldWithPath("optionGroups[].optionTypeId").description("상품 옵션 타입 Id"),
                fieldWithPath("optionGroups[].name").description("상품 옵션 타입 이름"),
                fieldWithPath("optionGroups[].priority").description("상품 옵션 우선순위"),
                fieldWithPath("optionGroups[].values[].optionValueId").description("상품 옵션 값 ID"),
                fieldWithPath("optionGroups[].values[].name").description("상품 옵션 값 이름"),
                fieldWithPath("images[].imageId").description("상품 이미지 Id"),
                fieldWithPath("images[].imagePath").description("상품 이미지 URL"),
                fieldWithPath("images[].sortOrder").description("상품 이미지 순서"),
                fieldWithPath("images[].thumbnail").description("썸네일 여부"),
                fieldWithPath("descriptionImages[].imageId").description("상품 설명 이미지 ID"),
                fieldWithPath("descriptionImages[].imagePath").description("상품 설명 이미지 URL"),
                fieldWithPath("descriptionImages[].sortOrder").description("상품 설명 이미지 순서"),
                fieldWithPath("variants[].variantId").description("상품 변형 ID"),
                fieldWithPath("variants[].sku").description("상품 변형 SKU"),
                fieldWithPath("variants[].optionValueIds").description("상품 변형 옵션 값 Id 리스트"),
                fieldWithPath("variants[].originalPrice").description("상품 변형 원본 가격"),
                fieldWithPath("variants[].discountedPrice").description("상품 변형 할인 가격"),
                fieldWithPath("variants[].discountRate").description("상품 변형 할인율"),
                fieldWithPath("variants[].stockQuantity").description("상품 변형 재고 수량")
        };
        //when
        //then
        mockMvc.perform(get("/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document("03-product-08-get-product",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                resource(
                                        ResourceSnippetParameters.builder()
                                                .tag(TAG)
                                                .summary("상품 상세 조회")
                                                .description("상품 상세 정보를 조회한다")
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
    @DisplayName("상품 정보를 수정한다")
    void updateProduct() throws Exception {
        //given
        UpdateRequest request = fixtureMonkey.giveMeBuilder(UpdateRequest.class)
                .set("name", "새 이름")
                .set("categoryId", 1L)
                .set("description", "상품 설명")
                .sample();
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
                .andDo(createSecuredDocument("03-product-09-update",
                        "상품 정보 수정",
                        "상품 기본 정보를 수정한다",
                        ProductDescriptor.getUpdateRequest(),
                        ProductDescriptor.getUpdateResponse(),
                        parameterWithName("productId").description("수정할 상품 ID"))
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
                .andDo(createSecuredDocument("03-product-10-delete",
                                "상품 삭제",
                                "상품을 삭제한다",
                                parameterWithName("productId").description("삭제할 상품 ID"))
                );
    }
}
