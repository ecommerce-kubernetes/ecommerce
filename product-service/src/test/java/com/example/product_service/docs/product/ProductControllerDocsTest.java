package com.example.product_service.docs.product;

import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.product.adapter.in.web.ProductController;
import com.example.product_service.product.adapter.in.web.dto.request.*;
import com.example.product_service.product.application.service.ProductService;
import com.example.product_service.product.application.service.dto.command.ProductCommand;
import com.example.product_service.product.application.service.dto.result.ProductResult;
import com.example.product_service.product.domain.model.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.product_service.docs.descriptor.ProductDescriptor.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerDocsTest extends RestDocsSupport {
    ProductService productService = Mockito.mock(ProductService.class);

    @Override
    protected Object initController() {
        return new ProductController(productService);
    }

    @Test
    @DisplayName("상품을 생성한다")
    void createProduct() throws Exception {
        //given
        CreateProductRequest request = CreateProductRequest.builder()
                .name("상품")
                .categoryId(1L)
                .description("상품 설명")
                .build();
        ProductResult.Create result = ProductResult.Create.builder()
                .productId(1L)
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.createProduct(any(ProductCommand.Create.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isCreated())
                .andDo(document(
                        "products",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(getCreateRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getCreateResponse())
                ));
    }

    @Test
    @DisplayName("상품 옵션 정의")
    void registerProductOption() throws Exception {
        //given
        RegisterProductOptionRequest request = RegisterProductOptionRequest.builder()
                .optionTypeIds(
                        List.of(1L)
                ).build();

        ProductResult.OptionRegister result = ProductResult.OptionRegister.builder()
                .productId(1L)
                .options(List.of(
                        ProductResult.Option.builder()
                                    .optionTypeId(1L)
                                    .optionTypeName("사이즈")
                                    .priority(1)
                                    .build()))
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.defineOptions(any(ProductCommand.OptionRegister.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(put("/products/{productId}/options", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/options",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(getRegisterOptionRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getRegisterOptionResponse())
                ));
    }

    @Test
    @DisplayName("상품 변형 추가")
    void addVariants() throws Exception {
        //given
        AddProductVariantRequest request = AddProductVariantRequest.builder()
                .variants(
                        List.of(
                                ProductVariantDetailRequest.builder()
                                        .originalPrice(10000L)
                                        .discountRate(10)
                                        .stockQuantity(100)
                                        .optionValueIds(List.of(1L,2L))
                                        .build()
                        )
                ).build();
        ProductResult.AddVariant result = ProductResult.AddVariant.builder()
                .productId(1L)
                .variants(
                        List.of(
                                ProductResult.VariantDetail.builder()
                                        .variantId(1L)
                                        .sku("PROD1_XL_BLUE")
                                        .optionValueIds(List.of(1L,2L))
                                        .originalPrice(10000L)
                                        .discountedPrice(9000L)
                                        .discountRate(10)
                                        .stockQuantity(100)
                                        .build()
                        )
                ).build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.createVariants(any(ProductCommand.AddVariant.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(post("/products/{productId}/variants", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/variants",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(getAddVariantRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getAddVariantResponse())
                ));
    }

    @Test
    @DisplayName("상품 이미지 추가")
    void updateImages() throws Exception {
        //given
        AddProductImageRequest request = AddProductImageRequest.builder()
                .images(List.of("/test/image.jpg"))
                .build();
        ProductResult.AddImage result = ProductResult.AddImage.builder()
                .productId(1L)
                .images(
                        List.of(
                                ProductResult.ImageDetail.builder()
                                        .imageId(1L)
                                        .imagePath("/test/image.jpg")
                                        .isThumbnail(true)
                                        .sortOrder(1)
                                        .build()
                        )
                ).build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.updateImages(any(ProductCommand.AddImage.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(put("/products/{productId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/images",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(getAddImageRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getAddImageResponse())
                ));
    }

    @Test
    @DisplayName("상품 설명 이미지 추가")
    void updateDescriptionImage() throws Exception {
        AddProductDescriptionImageRequest request = AddProductDescriptionImageRequest.builder()
                .images(
                        List.of("/test/image.jpg")
                ).build();
        ProductResult.AddDescriptionImage result = ProductResult.AddDescriptionImage.builder()
                .productId(1L)
                .images(
                        List.of(ProductResult.DescriptionImageDetail.builder()
                                .imageId(1L)
                                .imagePath("/test/image.jpg")
                                .sortOrder(1)
                                .build())
                ).build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.updateDescriptionImages(any(ProductCommand.AddDescriptionImage.class)))
                .willReturn(result);
        mockMvc.perform(put("/products/{productId}/description-images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/description-images",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(getAddDescriptionImageRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getAddDescriptionImageResponse())
                ));
    }

    @Test
    @DisplayName("상품을 게시한다")
    void publishProduct() throws Exception {
        //given
        ProductResult.Publish result = ProductResult.Publish.builder()
                .productId(1L)
                .status(ProductStatus.ON_SALE)
                .publishedAt(LocalDateTime.now())
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.publish(anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/publish", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/publish",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getPublishResponse())
                ));
    }

    @Test
    @DisplayName("상품을 판매 중지로 변경한다")
    void closeProduct() throws Exception {
        //given
        ProductResult.Close result = ProductResult.Close.builder()
                .productId(1L)
                .status(ProductStatus.STOP_SALE)
                .saleStoppedAt(LocalDateTime.now())
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.closedProduct(anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/close", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/close",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getCloseResponse())
                ));
    }

    @Test
    @DisplayName("상품 목록을 조회한다")
    void getProducts() throws Exception {
        //given
        ProductResult.Summary summary = mockSummaryResult();
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductResult.Summary> results = new PageImpl<>(List.of(summary), pageable, 100L);
        given(productService.getProducts(any(ProductCommand.Search.class)))
                .willReturn(results);
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
                .andDo(document(
                        "products/list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(getSearchParams()),
                        responseFields(getSummaryResponse())
                ));
    }

    @Test
    @DisplayName("상품을 조회한다")
    void getProduct() throws Exception {
        //given
        ProductResult.Detail result = mockDetailResult();
        given(productService.getProduct(anyLong()))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(get("/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(getDetailResponse())
                ));
    }

    @Test
    @DisplayName("상품 정보를 수정한다")
    void updateProduct() throws Exception {
        //given
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("새 이름")
                .categoryId(1L)
                .description("상품 설명")
                .build();
        ProductResult.Update result = ProductResult.Update.builder()
                .productId(1L)
                .name("새 상품")
                .description("상품 설명")
                .categoryId(1L)
                .build();
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        given(productService.updateProduct(any(ProductCommand.Update.class)))
                .willReturn(result);
        //when
        //then
        mockMvc.perform(put("/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(
                        "products/update",
                        preprocessRequest(
                                prettyPrint(),
                                modifyHeaders()
                                        .remove("X-User-Id")
                                        .remove("X-User-Role")
                        ),
                        preprocessResponse(prettyPrint()),
                        requestFields(getUpdateRequest()),
                        requestHeaders(AUTH_HEADER),
                        responseFields(getUpdateResponse())
                ));
    }

    @Test
    @DisplayName("상품을 삭제한다")
    void deleteProduct() throws Exception {
        //given
        willDoNothing().given(productService).deleteProduct(anyLong());
        HttpHeaders authHeader = createAuthHeader("ROLE_ADMIN");
        //when
        //then
        mockMvc.perform(delete("/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(authHeader))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(document(
                        "products/delete",
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

    private ProductResult.Summary mockSummaryResult() {
        return ProductResult.Summary.builder()
                .productId(1L)
                .name("상품")
                .thumbnail("/test/image.jpg")
                .displayPrice(2700L)
                .originalPrice(3000L)
                .maxDiscountRate(10)
                .categoryId(1L)
                .publishedAt(LocalDateTime.now())
                .rating(3D)
                .reviewCount(100L)
                .status(ProductStatus.ON_SALE)
                .build();
    }

    private ProductResult.Detail mockDetailResult() {
        return ProductResult.Detail.builder()
                .productId(1L)
                .name("상품")
                .status(ProductStatus.ON_SALE)
                .categoryId(1L)
                .displayPrice(2700L)
                .originalPrice(3000L)
                .maxDiscountRate(10)
                .rating(3D)
                .reviewCount(100L)
                .optionGroups(
                        List.of(
                                ProductResult.OptionGroup.builder()
                                        .optionTypeId(1L)
                                        .name("사이즈")
                                        .values(
                                                List.of(
                                                        ProductResult.OptionValueDetail.builder()
                                                                .optionValueId(1L)
                                                                .name("XL").build()
                                                )
                                        )
                                        .build()))
                .images(
                        List.of(
                                ProductResult.ImageDetail.builder()
                                        .imagePath("/test/image.jpg")
                                        .sortOrder(1)
                                        .isThumbnail(true)
                                        .build()))
                .descriptionImages(
                        List.of(
                                ProductResult.DescriptionImageDetail.builder()
                                        .imagePath("/test/description.jpg")
                                        .sortOrder(1)
                                        .build()
                        )
                )
                .variants(
                        List.of(
                                ProductResult.VariantDetail.builder()
                                        .variantId(1L)
                                        .sku("PROD-XL")
                                        .optionValueIds(List.of(1L))
                                        .originalPrice(3000L)
                                        .discountedPrice(2700L)
                                        .discountRate(10)
                                        .stockQuantity(100).build()
                        )
                ).build();
    }
}
