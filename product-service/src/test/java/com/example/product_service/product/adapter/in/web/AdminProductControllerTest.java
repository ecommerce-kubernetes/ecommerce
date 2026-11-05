package com.example.product_service.product.adapter.in.web;

import com.example.product_service.product.adapter.in.web.dto.request.*;
import com.example.product_service.product.application.service.ProductCommandService;
import com.example.product_service.product.application.service.dto.command.*;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static com.example.product_service.product.fixture.ProductRequestFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestSecurityConfig.class)
@WebMvcTest(controllers = AdminProductController.class)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductCommandService productCommandService;

    @Test
    @DisplayName("상품을 생성한다")
    @WithCustomMockUser
    void createProduct() throws Exception {
        //given
        CreateProductRequest request = anCreateProductRequest().build();
        Long productId = 1L;
        given(productCommandService.createProduct(any(CreateProductCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(post("/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(String.valueOf(productId)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidCreateRequest")
    @DisplayName("상품 생성 요청 검증")
    @WithCustomMockUser
    void createProductValidation(String description, CreateProductRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/products"));
    }

    @Test
    @DisplayName("상품을 수정한다")
    @WithCustomMockUser
    void updateProduct() throws Exception {
        //given
        Long productId = 1L;
        UpdateProductRequest request = anUpdateProductRequest().build();
        given(productCommandService.updateProduct(any(UpdateProductCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(String.valueOf(productId)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidUpdateRequest")
    @DisplayName("상품 수정 요청 검증")
    @WithCustomMockUser
    void updateProductValidation(String description, UpdateProductRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long productId = 1L;
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/products/" + productId));
    }

    @Test
    @DisplayName("상품을 삭제한다")
    @WithCustomMockUser
    void deleteProduct() throws Exception {
        //given
        Long productId = 1L;
        //when
        //then
        mockMvc.perform(delete("/admin/products/{productId}", productId))
                .andDo(print())
                .andExpect(status().isNoContent());
        verify(productCommandService).deleteProduct(productId);
    }

    @Test
    @DisplayName("상품 옵션을 등록한다")
    @WithCustomMockUser
    void registerProductOptions() throws Exception {
        //given
        Long productId = 1L;
        RegisterProductOptionRequest request = anRegisterProductOptionRequest().build();
        given(productCommandService.registerProductOptions(any(RegisterProductOptionCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/options", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(String.valueOf(productId)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidRegisterOptionsRequest")
    @DisplayName("상품 옵션 등록 요청 검증")
    @WithCustomMockUser
    void registerProductOptionsValidation(String description, RegisterProductOptionRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long productId = 1L;
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/options", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/products/" + productId + "/options"));
    }

    @Test
    @DisplayName("상품 변형을 추가한다")
    @WithCustomMockUser
    void addProductVariants() throws Exception {
        //given
        Long productId = 1L;
        AddProductVariantRequest request = anAddProductVariantRequest().build();
        given(productCommandService.addProductVariants(any(AddProductVariantCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(post("/admin/products/{productId}/variants", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(String.valueOf(productId)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidAddVariantsRequest")
    @DisplayName("상품 변형 추가 요청 검증")
    @WithCustomMockUser
    void addProductVariantsValidation(String description, AddProductVariantRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long productId = 1L;
        //when
        //then
        mockMvc.perform(post("/admin/products/{productId}/variants", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/products/" + productId + "/variants"));
    }

    @Test
    @DisplayName("상품 이미지를 추가한다")
    @WithCustomMockUser
    void addProductImages() throws Exception {
        //given
        Long productId = 1L;
        AddProductImageRequest request = anAddProductImageRequest().build();
        given(productCommandService.addProductImages(any(AddProductImageCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(String.valueOf(productId)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidImagesRequest")
    @DisplayName("상품 이미지 추가 요청 검증")
    @WithCustomMockUser
    void addProductImagesValidation(String description, AddProductImageRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long productId = 1L;
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/products/" + productId + "/images"));
    }

    @Test
    @DisplayName("상품 설명 이미지를 추가한다")
    @WithCustomMockUser
    void addProductDescriptionImages() throws Exception {
        //given
        Long productId = 1L;
        AddProductDescriptionImageRequest request = anAddProductDescriptionImageRequest().build();
        given(productCommandService.addProductDescriptionImages(any(AddProductDescriptionImageCommand.class))).willReturn(productId);
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/description-images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(String.valueOf(productId)));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidDescriptionImagesRequest")
    @DisplayName("상품 설명 이미지 추가 요청 검증")
    @WithCustomMockUser
    void addProductDescriptionImagesValidation(String description, AddProductDescriptionImageRequest request, String expectedField, String expectedMessage) throws Exception {
        //given
        Long productId = 1L;
        //when
        //then
        mockMvc.perform(put("/admin/products/{productId}/description-images", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("errors[0].field").value(expectedField))
                .andExpect(jsonPath("errors[0].reason").value(expectedMessage))
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("path").value("/admin/products/" + productId + "/description-images"));
    }

    private static Stream<Arguments> provideInvalidCreateRequest() {
        return Stream.of(
                Arguments.of("이름이 누락되면 예외가 발생한다",
                        anCreateProductRequest().name(null).build(),
                        "name", "상품 이름은 필수 입니다"),
                Arguments.of("카테고리 ID가 누락되면 예외가 발생한다",
                        anCreateProductRequest().categoryId(null).build(),
                        "categoryId", "카테고리 ID는 필수 입니다")
        );
    }

    private static Stream<Arguments> provideInvalidUpdateRequest() {
        return Stream.of(
                Arguments.of("이름이 누락되면 예외가 발생한다",
                        anUpdateProductRequest().name(null).build(),
                        "name", "상품 이름은 필수 입니다"),
                Arguments.of("카테고리 ID가 누락되면 예외가 발생한다",
                        anUpdateProductRequest().categoryId(null).build(),
                        "categoryId", "카테고리 ID는 필수 입니다")
        );
    }

    private static Stream<Arguments> provideInvalidRegisterOptionsRequest() {
        return Stream.of(
                Arguments.of("옵션 타입 ID가 중복되면 예외가 발생한다",
                        anRegisterProductOptionRequest().optionTypeIds(List.of(1L, 1L)).build(),
                        "optionTypeIds", "옵션 타입 ID는 중복될 수 없습니다")
        );
    }

    private static Stream<Arguments> provideInvalidAddVariantsRequest() {
        return Stream.of(
                Arguments.of("상품 변형 가격이 100 미만이면 예외가 발생한다",
                        anAddProductVariantRequest()
                                .variants(List.of(anProductVariantDetailRequest().originalPrice(50L).build()))
                                .build(),
                        "variants[0].originalPrice", "상품 변형 가격은 100 이상이어야 합니다"),
                Arguments.of("할인율이 음수면 예외가 발생한다",
                        anAddProductVariantRequest()
                                .variants(List.of(anProductVariantDetailRequest().discountRate(-1).build()))
                                .build(),
                        "variants[0].discountRate", "할인율은 0 이상이어야 합니다"),
                Arguments.of("할인율이 100을 초과하면 예외가 발생한다",
                        anAddProductVariantRequest()
                                .variants(List.of(anProductVariantDetailRequest().discountRate(101).build()))
                                .build(),
                        "variants[0].discountRate", "할인율은 100 이하여야 합니다"),
                Arguments.of("재고 수량이 1 미만이면 예외가 발생한다",
                        anAddProductVariantRequest()
                                .variants(List.of(anProductVariantDetailRequest().stockQuantity(0).build()))
                                .build(),
                        "variants[0].stockQuantity", "재고 수량은 1 이상이어야 합니다"),
                Arguments.of("옵션 값 ID가 중복되면 예외가 발생한다",
                        anAddProductVariantRequest()
                                .variants(List.of(anProductVariantDetailRequest().optionValueIds(List.of(1L, 1L)).build()))
                                .build(),
                        "variants[0].optionValueIds", "옵션 값 ID는 중복될 수 없습니다")
        );
    }

    private static Stream<Arguments> provideInvalidImagesRequest() {
        return Stream.of(
                Arguments.of("이미지가 비어있으면 예외가 발생한다",
                        anAddProductImageRequest().images(List.of()).build(),
                        "images", "최소 1장의 이미지를 등록해야 합니다")
        );
    }

    private static Stream<Arguments> provideInvalidDescriptionImagesRequest() {
        return Stream.of(
                Arguments.of("이미지가 비어있으면 예외가 발생한다",
                        anAddProductDescriptionImageRequest().images(List.of()).build(),
                        "images", "최소 1장의 이미지를 등록해야 합니다")
        );
    }
}
