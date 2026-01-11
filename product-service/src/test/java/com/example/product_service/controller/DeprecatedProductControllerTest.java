package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.common.advice.ErrorResponseEntityFactory;
import com.example.product_service.config.TestConfig;
import com.example.product_service.controller.util.validator.PageableValidatorFactory;
import com.example.product_service.controller.util.validator.ProductPageableValidator;
import com.example.product_service.controller.util.validator.ReviewPageableValidator;
import com.example.product_service.dto.ProductSearch;
import com.example.product_service.dto.request.image.AddImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.product.UpdateProductBasicRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.dto.response.PageDto;
import com.example.product_service.dto.response.ReviewResponse;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.dto.response.options.ProductOptionTypeResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.product.ProductSummaryResponse;
import com.example.product_service.dto.response.product.ProductUpdateResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.DomainType;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ProductApplicationService;
import com.example.product_service.service.ProductQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.ControllerTestHelper.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeprecatedProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ErrorResponseEntityFactory.class, TestConfig.class})
class DeprecatedProductControllerTest {

    private static final String BASE_PATH = "/products";
    private static final String ID_PATH = BASE_PATH + "/1";
    private static final String POPULAR_PATH = BASE_PATH + "/popular";
    private static final String PRODUCT_IMAGE_PATH = BASE_PATH + "/1/images";
    private static final String PRODUCT_IMAGE_BULK_PATH = PRODUCT_IMAGE_PATH + "/bulk";
    private static final String PRODUCT_VARIANT_PATH = BASE_PATH + "/1/variants";
    private static final String REVIEW_PATH = BASE_PATH + "/1/reviews";
    private static final String IMAGE_URL = "http://test.jpg";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    PageableValidatorFactory pageableValidatorFactory;
    @MockitoBean
    ProductApplicationService productApplicationService;
    @MockitoBean
    ProductQueryService productQueryService;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage(NOT_FOUND)).thenReturn("NotFound");
        when(ms.getMessage(BAD_REQUEST)).thenReturn("BadRequest");
        when(ms.getMessage(BAD_REQUEST_VALIDATION)).thenReturn("Validation Error");
        when(ms.getMessage(CONFLICT)).thenReturn("Conflict");
    }

    @Test
    @DisplayName("상품 저장 테스트-성공")
    void createProductTest_success() throws Exception {
        ProductRequest request = createProductRequest();
        ProductResponse response = createProductResponse();
        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(검증)")
    void createProductTest_validation() throws Exception {
        ProductRequest request = new ProductRequest("", "", null,
                List.of("invalid"),
                List.of(new ProductOptionTypeRequest(null, -1)),
                List.of(new ProductVariantRequest("", -1, 0, 101, List.of())));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), BASE_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리 없음)")
    void createProductTest_notFound_category() throws Exception {
        ProductRequest request = createProductRequest();
        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 타입 없음)")
    void createProductTest_notFound_optionType() throws Exception {
        ProductRequest request = createProductRequest();
        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new NotFoundException(getMessage(OPTION_TYPE_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(OPTION_TYPE_NOT_FOUND), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 값 없음)")
    void createProductTest_notFound_optionValue() throws Exception {
        ProductRequest request = createProductRequest();
        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new NotFoundException(getMessage(OPTION_VALUE_NOT_FOUND)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(OPTION_VALUE_NOT_FOUND), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 값이 상품 옵션 타입의 옵션 값에 속하지 않는경우)")
    void createProductTest_notMatchType() throws Exception {
        ProductRequest request = createProductRequest();
        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(하나의 옵션타입당 하나의 옵션 값만 설정 가능)")
    void createProductTest_cardinality_violation() throws Exception {
        ProductRequest request = createProductRequest();
        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 옵션에 중복된 아이디가 있는 경우)")
    void createProductTest_duplicate_productOptionType_id() throws Exception {
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 1),
                        new ProductOptionTypeRequest(1L,2)));

        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(상품 옵션에 중복된 priority 가 있는 경우")
    void createProductTest_duplicate_productOptionType_priority() throws Exception {
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 1),
                        new ProductOptionTypeRequest(2L, 1))
        );

        when(productApplicationService.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST)));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST), BASE_PATH);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-성공")
    void addImagesTest_success() throws Exception {
        AddImageRequest request = new AddImageRequest(List.of("http://test1.jpg", "http://test2.jpg"));
        List<ImageResponse> response = List.of(new ImageResponse(1L, "http://test1.jpg", 2),
                new ImageResponse(1L, "http://test2.jpg", 3));
        when(productApplicationService.addImages(anyLong(), any(AddImageRequest.class)))
                .thenReturn(response);
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_IMAGE_BULK_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-실패(검증)")
    void addImagesTest_validation() throws Exception {
        AddImageRequest request = new AddImageRequest(List.of());
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_IMAGE_BULK_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), PRODUCT_IMAGE_BULK_PATH);
    }

    @Test
    @DisplayName("상품 이미지 추가 테스트-실패(상품 없음)")
    void addImagesTest_notFound() throws Exception {
        AddImageRequest request = new AddImageRequest(List.of("http://test1.jpg", "http://test2.jpg"));
        when(productApplicationService.addImages(anyLong(),any(AddImageRequest.class)))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_IMAGE_BULK_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(PRODUCT_NOT_FOUND), PRODUCT_IMAGE_BULK_PATH);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-성공")
    void addVariantTest_success() throws Exception {
        ProductVariantRequest request =createProductVariantRequest();
        ProductVariantResponse response = new ProductVariantResponse(1L, "sku", 1000, 10, 10,
                List.of(new OptionValueResponse(1L, 1L, "value1"),
                        new OptionValueResponse(2L, 1L, "value2")));
        when(productApplicationService.addVariant(anyLong(), any(ProductVariantRequest.class)))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_VARIANT_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(검증)")
    void addVariantTest_validation() throws Exception {
        ProductVariantRequest request = new ProductVariantRequest();
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_VARIANT_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), PRODUCT_VARIANT_PATH);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(상품 없음)")
    void addVariantTest_notFound() throws Exception {
        ProductVariantRequest request = createProductVariantRequest();
        when(productApplicationService.addVariant(anyLong(), any(ProductVariantRequest.class)))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_VARIANT_NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_VARIANT_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(PRODUCT_VARIANT_NOT_FOUND), PRODUCT_VARIANT_PATH);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(옵션 값이 상품 옵션 타입의 옵션 값에 속하지 않는경우)")
    void addVariantTest_notMatchType() throws Exception {
        ProductVariantRequest request = createProductVariantRequest();
        when(productApplicationService.addVariant(anyLong(), any(ProductVariantRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE)));
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_VARIANT_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_VALUE_NOT_MATCH_TYPE), PRODUCT_VARIANT_PATH);
    }

    @Test
    @DisplayName("상품 변형 추가 테스트-실패(하나의 옵션타입당 하나의 옵션 값만 설정 가능)")
    void addVariantTest_cardinality_violation() throws Exception {
        ProductVariantRequest request = createProductVariantRequest();
        when(productApplicationService.addVariant(anyLong(), any(ProductVariantRequest.class)))
                .thenThrow(new BadRequestException(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION)));
        ResultActions perform = performWithBody(mockMvc, post(PRODUCT_VARIANT_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION), PRODUCT_VARIANT_PATH);
    }

    @Test
    @DisplayName("상품 조회 테스트-성공")
    void getProductsTest_success() throws Exception {
        PageDto<ProductSummaryResponse> response = new PageDto<>(
                createProductSummaryResponse(),
                0, 10, 10, 100);
        when(pageableValidatorFactory.getValidator(DomainType.PRODUCT)).thenReturn(new ProductPageableValidator());
        when(productQueryService.getProducts(any(ProductSearch.class), any(Pageable.class)))
                .thenReturn(response);
        ResultActions perform = performWithPageRequest(mockMvc, get(BASE_PATH), 0,
                10, List.of("id,asc"), Map.of("categoryId", "3", "rating", "5"));
        verifySuccessResponse(perform, status().isOk(), response);

    }

    @Test
    @DisplayName("상품 조회 테스트-실패(검증)")
    void getProductTest_validation() throws Exception {
        ResultActions perform = performWithPageRequest(mockMvc, get(BASE_PATH), 0, 10, List.of("id,asc"),
                Map.of("categoryId", "-1", "rating", ""));
        verifyErrorResponse(perform, status().isBadRequest(),
                getMessage(BAD_REQUEST), getMessage(BAD_REQUEST_VALIDATION), BASE_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @Test
    @DisplayName("상품 상세 조회 테스트-성공")
    void getProductTest_success() throws Exception {
        ProductResponse response = createProductResponse();
        when(productQueryService.getProductById(anyLong()))
                .thenReturn(response);
        ResultActions perform = performWithBody(mockMvc, get(ID_PATH), null);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("상품 상세 조회 테스트-실패(없음)")
    void getProductTest_notFound() throws Exception {
        when(productQueryService.getProductById(anyLong()))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, get(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(),
                getMessage(NOT_FOUND), getMessage(PRODUCT_NOT_FOUND),ID_PATH);
    }

    @Test
    @DisplayName("인기 상품 조회 테스트-성공")
    void getPopularProductsTest_success() throws Exception {
        PageDto<ProductSummaryResponse> response =
                new PageDto<>(createProductSummaryResponse(), 0, 10, 10, 100);
        when(productQueryService.getPopularProducts(anyInt(), anyInt(), anyLong()))
                .thenReturn(response);
        ResultActions perform = performWithParams(mockMvc, get(POPULAR_PATH), new LinkedMultiValueMap<>() {{
            add("page", "0");
            add("size", "10");
            add("categoryId", "1");
        }});
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("상품 리뷰 조회 테스트-성공")
    void getReviewsByProductIdTest_success() throws Exception {
        PageDto<ReviewResponse> response = new PageDto<>(createReviewResponse(), 0, 10, 10, 100);
        when(productQueryService.getReviewsByProductId(anyLong(), any(Pageable.class)))
                .thenReturn(response);
        when(pageableValidatorFactory.getValidator(DomainType.REVIEW)).thenReturn(new ReviewPageableValidator());

        ResultActions perform = performWithBody(mockMvc, get(REVIEW_PATH), null);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("상품 리뷰 조회 테스트-실패(상품 없음)")
    void getReviewsByProductIdTest_notFound() throws Exception {
        when(productQueryService.getReviewsByProductId(anyLong(), any(Pageable.class)))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_NOT_FOUND)));
        when(pageableValidatorFactory.getValidator(DomainType.REVIEW)).thenReturn(new ReviewPageableValidator());

        ResultActions perform = performWithBody(mockMvc, get(REVIEW_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(PRODUCT_NOT_FOUND), REVIEW_PATH);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-성공")
    void updateBasicInfoTest_success() throws Exception {
        UpdateProductBasicRequest request =
                new UpdateProductBasicRequest("updateName", "description", 1L);
        ProductUpdateResponse response = new ProductUpdateResponse(1L, "name", "description", 1L);
        when(productApplicationService.updateBasicInfoById(anyLong(), any(UpdateProductBasicRequest.class)))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifySuccessResponse(perform, status().isOk(), response);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(검증)")
    void updateBasicInfoTest_validation() throws Exception {
        UpdateProductBasicRequest request = new UpdateProductBasicRequest(" ", "description", 1L);
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage(BAD_REQUEST),
                getMessage(BAD_REQUEST_VALIDATION), ID_PATH);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(상품 없음)")
    void updateBasicInfoTest_product_notFound() throws Exception {
        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updatedName", "description", 1L);
        when(productApplicationService.updateBasicInfoById(anyLong(), any(UpdateProductBasicRequest.class)))
                .thenThrow(new NotFoundException(getMessage(NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(NOT_FOUND),ID_PATH);
    }

    @Test
    @DisplayName("상품 기본 정보 수정 테스트-실패(카테고리 없음)")
    void updateBasicInfoTest_category_notFound() throws Exception {
        UpdateProductBasicRequest request = new UpdateProductBasicRequest("updatedName", "description", 1L);
        when(productApplicationService.updateBasicInfoById(anyLong(), any(UpdateProductBasicRequest.class)))
                .thenThrow(new NotFoundException(getMessage(CATEGORY_NOT_FOUND)));
        ResultActions perform = performWithBody(mockMvc, patch(ID_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(CATEGORY_NOT_FOUND), ID_PATH);
    }

    @Test
    @DisplayName("상품 삭제 테스트-성공")
    void deleteProductTest_success() throws Exception {
        doNothing().when(productApplicationService).deleteProductById(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifySuccessResponse(perform, status().isNoContent(), null);
    }

    @Test
    @DisplayName("상품 삭제 테스트-실패(상품 없음)")
    void deleteProductTest_notFound() throws Exception {
        doThrow(new NotFoundException(getMessage(PRODUCT_NOT_FOUND)))
                .when(productApplicationService).deleteProductById(anyLong());
        ResultActions perform = performWithBody(mockMvc, delete(ID_PATH), null);
        verifyErrorResponse(perform, status().isNotFound(), getMessage(NOT_FOUND),
                getMessage(PRODUCT_NOT_FOUND), ID_PATH);
    }

    private ProductResponse createProductResponse() {
        return new ProductResponse(1L, "product", "description", 1L, 0L, 0.0,
                LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ImageResponse(1L, IMAGE_URL, 0)),
                List.of(new ProductOptionTypeResponse(1L, "optionType1")),
                List.of(new ProductVariantResponse(1L, "sku", 100, 10, 10,
                        List.of(new OptionValueResponse(1L, 1L, "value")))));
    }

    private ProductRequest createProductRequest() {
        return new ProductRequest("product", "description", 1L,
                List.of(IMAGE_URL),
                List.of(new ProductOptionTypeRequest(1L, 0)),
                List.of(new ProductVariantRequest("sku", 100, 10, 10, List.of(new VariantOptionValueRequest(1L, 1L)))));
    }

    private List<ProductSummaryResponse> createProductSummaryResponse(){
        return List.of(
                new ProductSummaryResponse(1L, "product1", "description", IMAGE_URL, 1L,
                        LocalDateTime.now(), 3.5, 100, 10000, 9000, 10),
                new ProductSummaryResponse(2L, "product2", "description", IMAGE_URL, 1L,
                        LocalDateTime.now(), 3.7, 100, 10000, 9000, 10)
        );
    }

    private List<ReviewResponse> createReviewResponse(){
        return List.of(
                new ReviewResponse(1L, "productName", 1L, "username", 4, "content",
                        List.of(new OptionValueResponse(1L, 1L, "value1"),
                                new OptionValueResponse(2L, 2L, "value2")), LocalDateTime.now())
        );
    }

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest("sku", 1000, 10, 10, List.of(new VariantOptionValueRequest(1L, 2L),
                new VariantOptionValueRequest(2L, 5L)));
    }
}