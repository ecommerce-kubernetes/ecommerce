package com.example.product_service.docs.product;

import com.example.product_service.api.common.dto.PageDto;
import com.example.product_service.api.product.controller.ProductController;
import com.example.product_service.api.product.controller.dto.request.ProductRequest;
import com.example.product_service.api.product.controller.dto.response.ProductResponse;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.service.ProductService;
import com.example.product_service.api.product.service.dto.command.ProductCommand;
import com.example.product_service.api.product.service.dto.result.ProductResult;
import com.example.product_service.docs.RestDocsSupport;
import com.example.product_service.docs.descriptor.ProductDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
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
        ProductRequest.Create request = ProductRequest.Create.builder()
                .name("상품")
                .categoryId(1L)
                .description("상품 설명")
                .build();
        ProductResult.Create result = ProductResult.Create.builder()
                .productId(1L)
                .build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.createProduct(any(ProductCommand.Create.class)))
                .willReturn(result);
        ProductResponse.Create response = ProductResponse.Create.from(result);
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
        ProductRequest.OptionRegister request = ProductRequest.OptionRegister.builder()
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
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.defineOptions(any(ProductCommand.OptionRegister.class)))
                .willReturn(result);
        ProductResponse.OptionRegister response = ProductResponse.OptionRegister.from(result);
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
        ProductRequest.AddVariant request = ProductRequest.AddVariant.builder()
                .variants(
                        List.of(
                                ProductRequest.VariantDetail.builder()
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
        ProductResponse.AddVariant response = ProductResponse.AddVariant.from(result);
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.createVariants(any(ProductCommand.AddVariant.class)))
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
        ProductRequest.AddImage request = ProductRequest.AddImage.builder()
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
        ProductResponse.AddImage response = ProductResponse.AddImage.from(result);
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.updateImages(any(ProductCommand.AddImage.class)))
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
        ProductRequest.AddDescriptionImage request = ProductRequest.AddDescriptionImage.builder()
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
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.updateDescriptionImages(any(ProductCommand.AddDescriptionImage.class)))
                .willReturn(result);
        ProductResponse.AddDescriptionImage response = ProductResponse.AddDescriptionImage.from(result);
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
        ProductResult.Publish result = ProductResult.Publish.builder()
                .productId(1L)
                .status(ProductStatus.ON_SALE)
                .publishedAt(LocalDateTime.now())
                .build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.publish(anyLong()))
                .willReturn(result);
        ProductResponse.Publish response = ProductResponse.Publish.from(result);
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
        ProductResult.Close result = ProductResult.Close.builder()
                .productId(1L)
                .status(ProductStatus.STOP_SALE)
                .saleStoppedAt(LocalDateTime.now())
                .build();
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.closedProduct(anyLong()))
                .willReturn(result);
        ProductResponse.Close response = ProductResponse.Close.from(result);
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
        ProductResult.Summary summary = mockSummaryResult();
        PageRequest pageable = PageRequest.of(0, 10);
        Page<ProductResult.Summary> results = new PageImpl<>(List.of(summary), pageable, 100L);
        given(productService.getProducts(any(ProductCommand.Search.class)))
                .willReturn(results);
        PageDto<ProductResponse.Summary> response = PageDto.of(results, ProductResponse.Summary::from);
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
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createPublicDocument("03-product-07-get-products",
                                "상품 목록 조회",
                                "상품 목록을 조회한다",
                                ProductDescriptor.getSummaryResponse(),
                                ProductDescriptor.getSearchParams())
                );
    }

    @Test
    @DisplayName("상품을 조회한다")
    void getProduct() throws Exception {
        //given
        ProductResult.Detail result = mockDetailResult();
        given(productService.getProduct(anyLong()))
                .willReturn(result);
        ProductResponse.Detail response = ProductResponse.Detail.from(result);
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
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
                .andDo(createPublicDocument("03-product-08-get-product",
                                "상품 상세 조회",
                                "상품 상세 정보를 조회한다",
                                ProductDescriptor.getDetailResponse(),
                                parameterWithName("productId").description("조회할 상품 ID"))
                );
    }

    @Test
    @DisplayName("상품 정보를 수정한다")
    void updateProduct() throws Exception {
        //given
        ProductRequest.Update request = ProductRequest.Update.builder()
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
        HttpHeaders adminHeader = createAdminHeader();
        given(productService.updateProduct(any(ProductCommand.Update.class)))
                .willReturn(result);
        ProductResponse.Update response = ProductResponse.Update.from(result);
        //when
        //then
        mockMvc.perform(put("/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .headers(adminHeader))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)))
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
                                        .imagePath("http://image.jpg")
                                        .sortOrder(1)
                                        .isThumbnail(true)
                                        .build()))
                .descriptionImages(
                        List.of(
                                ProductResult.DescriptionImageDetail.builder()
                                        .imagePath("http://description.jpg")
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
