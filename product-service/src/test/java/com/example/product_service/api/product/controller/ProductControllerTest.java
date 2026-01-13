package com.example.product_service.api.product.controller;

import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.api.product.controller.dto.ProductCreateRequest;
import com.example.product_service.api.product.controller.dto.ProductImageCreateRequest;
import com.example.product_service.api.product.controller.dto.ProductOptionSpecRequest;
import com.example.product_service.api.product.controller.dto.VariantCreateRequest;
import com.example.product_service.api.product.service.dto.command.AddVariantCommand;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.result.*;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Stream;

import static com.example.product_service.support.fixture.ProductControllerFixture.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
public class ProductControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("상품을 생성한다")
    @WithCustomMockUser
    void createProduct() throws Exception {
        //given
        ProductCreateRequest request = mockCreateRequest().build();
        ProductCreateResponse response = mockCreateResponse().build();
        given(productService.createProduct(any(ProductCreateCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("상품을 생성하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void createProduct_user_role() throws Exception {
        //given
        ProductCreateRequest request = mockCreateRequest().build();
        //when
        //then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 상품을 생성할 수 없다")
    void createProduct_unAuthorized() throws Exception {
        //given
        ProductCreateRequest request = mockCreateRequest().build();
        //when
        //then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidProductCreateRequest")
    @WithCustomMockUser
    @DisplayName("상품 저장 요청 검증")
    void createProduct_Validation(String description, ProductCreateRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products"));
    }

    @Test
    @DisplayName("상품 옵션을 정의한다")
    @WithCustomMockUser
    void addOptionSpec() throws Exception {
        //given
        ProductOptionSpecRequest request = mockOptionSpecRequest().build();
        ProductOptionSpecResponse response = mockOptionSpecResponse().build();
        given(productService.addOptionSpec(anyLong(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products/{productId}/option-specs", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("상품 옵션을 정의하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void addOptionSpec_user_role() throws Exception {
        //given
        ProductOptionSpecRequest request = mockOptionSpecRequest().build();
        //when
        //then
        mockMvc.perform(post("/products/{productId}/option-specs", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/option-specs"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 상품 옵션을 정의할 수 없다")
    void addOptionSpec_unAuthorized() throws Exception {
        //given
        ProductOptionSpecRequest request = mockOptionSpecRequest().build();
        //when
        //then
        mockMvc.perform(post("/products/{productId}/option-specs", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/option-specs"));
    }

    @Test
    @DisplayName("상품 옵션 정의시 옵션 Id 리스트는 필수이다")
    @WithCustomMockUser
    void addOptionSpec_invalidRequest() throws Exception {
        //given
        ProductOptionSpecRequest request = mockOptionSpecRequest().optionTypeIds(null).build();
        //when
        //then
        mockMvc.perform(post("/products/{productId}/option-specs", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("옵션 id 리스트는 필수 입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/option-specs"));
    }

    @Test
    @DisplayName("상품 변형을 추가한다")
    @WithCustomMockUser
    void addVariant() throws Exception {
        //given
        VariantCreateRequest request = mockCreateVariantRequest().build();
        VariantCreateResponse response = mockCreateVariantResponse().build();
        given(productService.addVariants(any(AddVariantCommand.class)))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(post("/products/{productId}/variants", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("상품 변형을 추가하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void addVariant_user_role() throws Exception {
        //given
        VariantCreateRequest request = mockCreateVariantRequest().build();
        //when
        //then
        mockMvc.perform(post("/products/{productId}/variants", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/variants"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 상품 변형을 추가할 수 없다")
    void addVariant_unAuthorized() throws Exception {
        //given
        VariantCreateRequest request = mockCreateVariantRequest().build();
        //when
        //then
        mockMvc.perform(post("/products/{productId}/variants", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/variants"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidAddVariantRequest")
    @DisplayName("상품 변형 추가 요청 검증")
    @WithCustomMockUser
    void addVariant_validation(String description, VariantCreateRequest request, String message) throws Exception {
        //given
        //when
        //then
        mockMvc.perform(post("/products/{productId}/variants", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/variants"));
    }

    @Test
    @DisplayName("상품 이미지를 추가한다")
    @WithCustomMockUser
    void addImage() throws Exception {
        //given
        ProductImageCreateRequest request = mockImageRequest().build();
        ProductImageCreateResponse response = mockImageResponse().build();
        given(productService.addImages(anyLong(), anyList()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(put("/products/{productId}/images", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("상품 이미지를 추가하려면 관리자 권한이여야한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void addImage_user_role() throws Exception {
        //given
        ProductImageCreateRequest request = mockImageRequest().build();
        //when
        //then
        mockMvc.perform(put("/products/{productId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/images"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 상품 이미지를 추가할 수 없다")
    void addImage_unAuthorized() throws Exception {
        //given
        ProductImageCreateRequest request = mockImageRequest().build();
        //when
        //then
        mockMvc.perform(put("/products/{productId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/images"));
    }

    @Test
    @DisplayName("상품 이미지 요청 검증")
    @WithCustomMockUser
    void addImage_invalidRequest() throws Exception {
        //given
        ProductImageCreateRequest request = mockImageRequest().images(null).build();
        //when
        //then
        mockMvc.perform(put("/products/{productId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("상품 이미지는 필수 입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/images"));
    }

    @Test
    @DisplayName("상품을 게시한다")
    @WithCustomMockUser
    void publishProduct() throws Exception {
        //given
        ProductPublishResponse response = mockPublishResponse().build();
        given(productService.publish(anyLong()))
                .willReturn(response);
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/publish", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @DisplayName("상품을 게시하려면 관리자 권한이여야 한다")
    @WithCustomMockUser(userRole = UserRole.ROLE_USER)
    void publishProduct_user_role() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/publish", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/publish"));
    }

    @Test
    @DisplayName("로그인 하지 않은 사용자는 상품을 게시할 수 없다")
    void publishProduct_unAuthorized() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(patch("/products/{productId}/publish", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/products/1/publish"));
    }

    private static Stream<Arguments> provideInvalidProductCreateRequest() {
        return Stream.of(
                Arguments.of("상품 이름이 공백", mockCreateRequest().name(null).build(), "상품 이름은 필수 입니다"),
                Arguments.of("카테고리 Id가 null", mockCreateRequest().categoryId(null).build(), "카테고리 id는 필수 입니다")
        );
    }

    private static Stream<Arguments> provideInvalidAddVariantRequest() {
        return Stream.of(
                Arguments.of("상품 변형 리스트가 null", mockCreateVariantRequest().variants(null).build(), "상품 변형 리스트는 필수입니다"),
                Arguments.of("상품 변형 가격이 null", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(null).discountRate(10).stockQuantity(100).optionValueIds(List.of()).build())
                ).build(), "가격은 필수 입니다"),
                Arguments.of("상품 변형 가격이 100 미만", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(99L).discountRate(10).stockQuantity(100).optionValueIds(List.of()).build())
                ).build(), "가격은 100 이상이여야 합니다"),
                Arguments.of("상품 변형 할인율이 null", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(100L).discountRate(null).stockQuantity(100).optionValueIds(List.of()).build())
                ).build(), "할인율은 필수 입니다"),
                Arguments.of("상품 변형 할인율이 0 미만", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(100L).discountRate(-1).stockQuantity(100).optionValueIds(List.of()).build())
                ).build(), "할인율은 0 이상이여야 합니다"),
                Arguments.of("상품 변형 할인율이 100 이상", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(100L).discountRate(101).stockQuantity(100).optionValueIds(List.of()).build())
                ).build(), "할인율은 100 이하여야 합니다"),
                Arguments.of("재고 수량이 null", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(100L).discountRate(10).stockQuantity(null).optionValueIds(List.of()).build())
                ).build(), "재고 수량은 필수 입니다"),
                Arguments.of("재고 수량이 0", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(100L).discountRate(10).stockQuantity(0).optionValueIds(List.of()).build())
                ).build(), "재고 수량은 1 이상이여야 합니다"),
                Arguments.of("상품 변형 옵션이 null", mockCreateVariantRequest().variants(
                        List.of(VariantCreateRequest.VariantRequest.builder().price(100L).discountRate(10).stockQuantity(100).optionValueIds(null).build())
                ).build(), "상품 변형 옵션은 필수 입니다")
        );
    }
}
