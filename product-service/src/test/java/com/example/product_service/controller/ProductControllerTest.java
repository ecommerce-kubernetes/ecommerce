package com.example.product_service.controller;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.dto.response.options.ProductOptionTypeResponse;
import com.example.product_service.dto.response.product.ProductResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.exception.NotFoundException;
import com.example.product_service.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.product_service.controller.util.ControllerTestHelper.*;
import static com.example.product_service.controller.util.TestMessageUtil.getMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    private static final String BASE_PATH = "/products";
    private static final String IMAGE_URL = "http://test.jpg";
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MessageSourceUtil ms;
    @MockitoBean
    ProductService service;

    @BeforeEach
    void setUpMessages() {
        when(ms.getMessage("badRequest")).thenReturn("BadRequest");
        when(ms.getMessage("badRequest.validation")).thenReturn("Validation Error");
        when(ms.getMessage("conflict")).thenReturn("Conflict");
    }

    @Test
    @DisplayName("상품 저장 테스트-성공")
    void createProductTest_success() throws Exception {
        ProductRequest request = createProductRequest();

        ProductResponse response = new ProductResponse(1L, "product", "description", 1L,
                LocalDateTime.now(), LocalDateTime.now(),
                List.of(new ImageResponse(1L, IMAGE_URL, 0)),
                List.of(new ProductOptionTypeResponse(1L, "optionType1")),
                List.of(new ProductVariantResponse(1L, "sku", 100, 10, 10,
                        List.of(new OptionValueResponse(1L, 1L, "value")))));

        when(service.saveProduct(any(ProductRequest.class)))
                .thenReturn(response);

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifySuccessResponse(perform, status().isCreated(), response);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(검증)")
    void createProductTest_validation() throws Exception {
        ProductRequest request = new ProductRequest("", "", null,
                List.of(new ImageRequest("invalid", -1)),
                List.of(new ProductOptionTypeRequest(null, -1)),
                List.of(new ProductVariantRequest("", -1, 0, 101, List.of())));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"),
                getMessage("badRequest.validation"), BASE_PATH);

        perform.andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(카테고리 없음)")
    void createProductTest_notFound_category() throws Exception {
        ProductRequest request = createProductRequest();
        when(service.saveProduct(any(ProductRequest.class)))
                .thenThrow(new NotFoundException(getMessage("category.notFound")));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("category.notFound"), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 타입 없음)")
    void createProductTest_notFound_optionType() throws Exception {
        ProductRequest request = createProductRequest();
        when(service.saveProduct(any(ProductRequest.class)))
                .thenThrow(new NotFoundException(getMessage("option-type.notFound")));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("option-type.notFound"), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 값 없음)")
    void createProductTest_notFound_optionValue() throws Exception {
        ProductRequest request = createProductRequest();
        when(service.saveProduct(any(ProductRequest.class)))
                .thenThrow(new NotFoundException(getMessage("option-value.notFound")));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isNotFound(), getMessage("notFound"),
                getMessage("option-value.notFound"), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(옵션 값이 상품 옵션 타입의 옵션 값에 속하지 않는경우)")
    void createProductTest_badRequest_invalidOptionValue() throws Exception {
        ProductRequest request = createProductRequest();
        when(service.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BadRequestException(getMessage("product.option-value.notMatchType")));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"),
                getMessage("product.option-value.notMatchType"), BASE_PATH);
    }

    @Test
    @DisplayName("상품 저장 테스트-실패(하나의 옵션타입당 하나의 옵션 값만 설정 가능)")
    void createProductTest_badRequest_singleOptionValuePerOptionType() throws Exception {
        ProductRequest request = createProductRequest();
        when(service.saveProduct(any(ProductRequest.class)))
                .thenThrow(new BadRequestException(getMessage("product.option-value.cardinality.violation")));

        ResultActions perform = performWithBody(mockMvc, post(BASE_PATH), request);
        verifyErrorResponse(perform, status().isBadRequest(), getMessage("badRequest"),
                getMessage("product.option-value.cardinality.violation"), BASE_PATH);
    }

    @Test
    @DisplayName("상품 조회 테스트-성공")
    void getProductsTest_success() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "10");
        params.add("sort", "id,desc");
        params.add("categoryId", "1");
        params.add("name", "name");
        params.add("rating", "4");
        ResultActions perform = performWithParams(mockMvc, get(BASE_PATH), params);


    }

    private ProductRequest createProductRequest() {
        return new ProductRequest("product", "description", 1L,
                List.of(new ImageRequest(IMAGE_URL, 0)),
                List.of(new ProductOptionTypeRequest(1L, 0)),
                List.of(new ProductVariantRequest("sku", 100, 10, 10, List.of(1L))));
    }
}