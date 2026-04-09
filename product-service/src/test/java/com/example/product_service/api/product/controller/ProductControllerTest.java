package com.example.product_service.api.product.controller;

import com.example.product_service.api.common.dto.PageDto;
import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.api.product.controller.dto.request.ProductRequest;
import com.example.product_service.api.product.controller.dto.response.ProductResponse;
import com.example.product_service.api.product.domain.model.ProductStatus;
import com.example.product_service.api.product.service.dto.command.ProductCommand;
import com.example.product_service.api.product.service.dto.result.ProductResult;
import com.example.product_service.support.ControllerTestSupport;
import com.example.product_service.support.security.annotation.WithCustomMockUser;
import com.example.product_service.support.security.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
class ProductControllerTest extends ControllerTestSupport {

    @Nested
    @DisplayName("상품 생성")
    class Create {
        @Test
        @DisplayName("상품을 생성한다")
        @WithCustomMockUser
        void createProduct() throws Exception {
            //given
            ProductRequest.Create request = fixtureMonkey.giveMeOne(ProductRequest.Create.class);
            ProductResult.Create result = fixtureMonkey.giveMeOne(ProductResult.Create.class);
            assert result != null;
            given(productService.createProduct(any(ProductCommand.Create.class)))
                    .willReturn(result);
            ProductResponse.Create response = ProductResponse.Create.from(result);
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
            ProductRequest.Create request = fixtureMonkey.giveMeOne(ProductRequest.Create.class);
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
            ProductRequest.Create request = fixtureMonkey.giveMeOne(ProductRequest.Create.class);
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
        void createProduct_Validation(String description, ProductRequest.Create request, String message) throws Exception {
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

        private static Stream<Arguments> provideInvalidProductCreateRequest() {
            return Stream.of(
                    Arguments.of("상품 이름이 공백", ProductRequest.Create.builder()
                                    .name(null).categoryId(1L).description("상품 설명").build(),
                            "상품 이름은 필수 입니다"),
                    Arguments.of("카테고리 Id가 null", ProductRequest.Create.builder()
                                    .name("상품").categoryId(null).description("상품 설명")
                                    .build(),
                            "카테고리 id는 필수 입니다")
            );
        }
    }

    @Nested
    @DisplayName("상품 옵션 설정")
    class RegisterProductOption {
        @Test
        @DisplayName("상품 옵션을 설정한다")
        @WithCustomMockUser
        void registerProductOption() throws Exception {
            //given
            ProductRequest.OptionRegister request = fixtureMonkey.giveMeOne(ProductRequest.OptionRegister.class);
            ProductResult.OptionRegister result = fixtureMonkey.giveMeOne(ProductResult.OptionRegister.class);
            assert result != null;
            ProductResponse.OptionRegister response = ProductResponse.OptionRegister.from(result);
            given(productService.defineOptions(any(ProductCommand.OptionRegister.class)))
                    .willReturn(result);
            //when
            //then
            mockMvc.perform(put("/products/{productId}/options", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("상품 옵션을 정의하려면 관리자 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_USER)
        void registerProductOption_user_role() throws Exception {
            //given
            ProductRequest.OptionRegister request = fixtureMonkey.giveMeOne(ProductRequest.OptionRegister.class);
            //when
            //then
            mockMvc.perform(put("/products/{productId}/options", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/options"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 상품 옵션을 정의할 수 없다")
        void registerProductOption_unAuthorized() throws Exception {
            //given
            ProductRequest.OptionRegister request = fixtureMonkey.giveMeOne(ProductRequest.OptionRegister.class);
            //when
            //then
            mockMvc.perform(put("/products/{productId}/options", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/options"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideInvalidOptionSpecRequest")
        @DisplayName("상품 옵션 설정 요청 검증")
        @WithCustomMockUser
        void registerProductOption_invalidRequest(String description, ProductRequest.OptionRegister request, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(put("/products/{productId}/options", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/options"));
        }

        private static Stream<Arguments> provideInvalidOptionSpecRequest() {
            return Stream.of(
                    //필수 필드 누락
                    Arguments.of(
                            "옵션 리스트가 null",
                            wrap(null),
                            "옵션 리스트는 필수 입니다"
                    ),
                    //요청 내부 중복 옵션
                    Arguments.of(
                            "중복된 옵션 타입 Id",
                            wrap(List.of(
                                    1L,1L
                            )),
                            "옵션 ID는 중복될 수 없습니다"
                    ),
                    //최대 옵션 개수 초과
                    Arguments.of(
                            "옵션 개수가 3개 이상",
                            wrap(List.of(
                                1L,2L,3L,4L
                            )),
                            "옵션은 최대 3개까지만 설정 가능합니다"
                    )
            );
        }

        private static ProductRequest.OptionRegister wrap(List<Long> optionTypeIds) {
            return ProductRequest.OptionRegister.builder()
                    .optionTypeIds(optionTypeIds)
                    .build();
        }
    }

    @Nested
    @DisplayName("상품 변형 추가")
    class CreateVariant {
        @Test
        @DisplayName("상품 변형을 추가한다")
        @WithCustomMockUser
        void createVariants() throws Exception {
            //given
            ProductRequest.AddVariant request = fixtureMonkey.giveMeOne(ProductRequest.AddVariant.class);
            ProductResult.AddVariant result = fixtureMonkey.giveMeOne(ProductResult.AddVariant.class);
            assert result != null;
            ProductResponse.AddVariant response = ProductResponse.AddVariant.from(result);
            given(productService.createVariants(any(ProductCommand.AddVariant.class)))
                    .willReturn(result);
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
        void createVariants_user_role() throws Exception {
            //given
            ProductRequest.AddVariant request = fixtureMonkey.giveMeOne(ProductRequest.AddVariant.class);
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
        void createVariants_unAuthorized() throws Exception {
            //given
            ProductRequest.AddVariant request = fixtureMonkey.giveMeOne(ProductRequest.AddVariant.class);
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
        @MethodSource("provideInvalidCreateVariantsRequest")
        @DisplayName("상품 변형 추가 요청 검증")
        @WithCustomMockUser
        void createVariants_validation(String description, ProductRequest.AddVariant request, String message) throws Exception {
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

        private static Stream<Arguments> provideInvalidCreateVariantsRequest() {
            ProductRequest.VariantDetail VALID_BASE_VARIANT =
                    ProductRequest.VariantDetail.builder()
                            .originalPrice(10000L)
                            .discountRate(10)
                            .stockQuantity(100)
                            .optionValueIds(List.of(1L, 2L))
                            .build();
            return Stream.of(
                    Arguments.of(
                            "상품 변형 리스트가 null",
                            wrap(null),
                            "상품 변형 리스트는 필수입니다"),
                    Arguments.of(
                            "상품 변형 가격이 null",
                            wrap(VALID_BASE_VARIANT.toBuilder().originalPrice(null).build()),
                            "가격은 필수 입니다"),
                    Arguments.of(
                            "상품 변형 가격이 100 미만",
                            wrap(VALID_BASE_VARIANT.toBuilder().originalPrice(99L).build()),
                            "가격은 100 이상이여야 합니다"),
                    Arguments.of(
                            "상품 변형 할인율이 null",
                            wrap(VALID_BASE_VARIANT.toBuilder().discountRate(null).build()),
                            "할인율은 필수 입니다"),
                    Arguments.of(
                            "상품 변형 할인율이 0 미만",
                            wrap(VALID_BASE_VARIANT.toBuilder().discountRate(-1).build()),
                            "할인율은 0 이상이여야 합니다"),
                    Arguments.of(
                            "상품 변형 할인율이 100 이상",
                            wrap(VALID_BASE_VARIANT.toBuilder().discountRate(101).build()),
                            "할인율은 100 이하여야 합니다"),
                    Arguments.of(
                            "재고 수량이 null",
                            wrap(VALID_BASE_VARIANT.toBuilder().stockQuantity(null).build()),
                            "재고 수량은 필수 입니다"),
                    Arguments.of(
                            "재고 수량이 0",
                            wrap(VALID_BASE_VARIANT.toBuilder().stockQuantity(0).build()),
                            "재고 수량은 1 이상이여야 합니다"),
                    Arguments.of(
                            "상품 변형 옵션이 null",
                            wrap(VALID_BASE_VARIANT.toBuilder().optionValueIds(null).build()),
                            "상품 변형 옵션은 필수 입니다"),
                    Arguments.of(
                            "중복된 상품 변형 옵션",
                            wrap(VALID_BASE_VARIANT.toBuilder().optionValueIds(List.of(1L,1L)).build()),
                            "중복된 옵션 종류가 포함되어 있습니다")
            );
        }

        private static ProductRequest.AddVariant wrap(ProductRequest.VariantDetail variantDetail) {
            return ProductRequest.AddVariant.builder()
                    .variants(variantDetail == null ? null : List.of(variantDetail))
                    .build();
        }
    }

    @Nested
    @DisplayName("상품 이미지 추가")
    class AddImage {
        @Test
        @DisplayName("상품 이미지를 추가한다")
        @WithCustomMockUser
        void addImage() throws Exception {
            //given
            ProductRequest.AddImage request = fixtureMonkey.giveMeOne(ProductRequest.AddImage.class);
            ProductResult.AddImage result = fixtureMonkey.giveMeOne(ProductResult.AddImage.class);
            assert result != null;
            ProductResponse.AddImage response = ProductResponse.AddImage.from(result);
            given(productService.updateImages(any(ProductCommand.AddImage.class)))
                    .willReturn(result);
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
            ProductRequest.AddImage request = fixtureMonkey.giveMeOne(ProductRequest.AddImage.class);
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
            ProductRequest.AddImage request = fixtureMonkey.giveMeOne(ProductRequest.AddImage.class);
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

        @ParameterizedTest(name = "{0}")
        @DisplayName("상품 이미지 추가 요청 검증")
        @MethodSource("provideInvalidAddImageRequest")
        @WithCustomMockUser
        void addImage_invalidRequest(String description, ProductRequest.AddImage request, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(put("/products/{productId}/images", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/images"));
        }

        private static Stream<Arguments> provideInvalidAddImageRequest() {
            return Stream.of(
                    Arguments.of(
                            "이미지 리스트가 null",
                            wrap(null),
                            "최소 1장의 이미지를 등록해야 합니다"
                    ),
                    Arguments.of(
                            "잘못된 형식의 path",
                            wrap(List.of("invalidPath")),
                            "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
                    )
            );
        }

        private static ProductRequest.AddImage wrap(List<String> imagePaths) {
            return ProductRequest.AddImage.builder()
                    .images(imagePaths)
                    .build();
        }
    }

    @Nested
    @DisplayName("상품 설명 이미지 추가")
    class AddDescriptionImage {
        @Test
        @DisplayName("상품 설명 이미지를 추가한다")
        @WithCustomMockUser
        void updateDescriptionImage() throws Exception {
            //given
            ProductRequest.AddDescriptionImage request = fixtureMonkey.giveMeOne(ProductRequest.AddDescriptionImage.class);
            ProductResult.AddDescriptionImage result = fixtureMonkey.giveMeOne(ProductResult.AddDescriptionImage.class);
            assert result != null;
            given(productService.updateDescriptionImages(any(ProductCommand.AddDescriptionImage.class)))
                    .willReturn(result);
            ProductResponse.AddDescriptionImage response = ProductResponse.AddDescriptionImage.from(result);
            //when
            //then
            mockMvc.perform(put("/products/{productId}/description-images", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("상품 설명 이미지를 추가하려면 관리자 권한이여야한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_USER)
        void updateDescriptionImage_user_role() throws Exception {
            //given
            ProductRequest.AddDescriptionImage request = fixtureMonkey.giveMeOne(ProductRequest.AddDescriptionImage.class);
            //when
            //then
            mockMvc.perform(put("/products/{productId}/description-images", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/description-images"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 상품 설명 이미지를 추가할 수 없다")
        void updateDescriptionImage_unAuthorized() throws Exception {
            //given
            ProductRequest.AddDescriptionImage request = fixtureMonkey.giveMeOne(ProductRequest.AddDescriptionImage.class);
            //when
            //then
            mockMvc.perform(put("/products/{productId}/description-images", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/description-images"));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("상품 설명 이미지 요청 검증")
        @MethodSource("provideInvalidDescriptionImageRequest")
        @WithCustomMockUser
        void updateDescriptionImage_Validation(String description, ProductRequest.AddDescriptionImage request, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(put("/products/{productId}/description-images", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/description-images"));
        }

        private static Stream<Arguments> provideInvalidDescriptionImageRequest() {
            return Stream.of(
                    Arguments.of(
                            "images 가 null",
                            wrap(null),
                            "최소 1장의 이미지를 등록해야 합니다"
                    ),
                    Arguments.of(
                            "잘못된 형식의 path",
                            wrap(List.of("invalidPath")),
                            "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
                    )
            );
        }

        private static ProductRequest.AddDescriptionImage wrap(List<String> images) {
            return ProductRequest.AddDescriptionImage.builder()
                    .images(images)
                    .build();
        }
    }

    @Nested
    @DisplayName("상품 게시")
    class Publish {
        @Test
        @DisplayName("상품을 게시한다")
        @WithCustomMockUser
        void publishProduct() throws Exception {
            //given
            ProductResult.Publish result = fixtureMonkey.giveMeOne(ProductResult.Publish.class);
            assert result != null;
            given(productService.publish(anyLong()))
                    .willReturn(result);
            ProductResponse.Publish response = ProductResponse.Publish.from(result);
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
    }

    @Nested
    @DisplayName("상품 판매 중지")
    class CloseProduct {
        @Test
        @DisplayName("상품을 판매 중지한다")
        @WithCustomMockUser
        void closeProduct() throws Exception {
            //given
            ProductResult.Close result = fixtureMonkey.giveMeOne(ProductResult.Close.class);
            assert result != null;
            given(productService.closedProduct(anyLong()))
                    .willReturn(result);
            ProductResponse.Close response = ProductResponse.Close.from(result);
            //when
            //then
            mockMvc.perform(patch("/products/{productId}/close", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("상품을 판매 중지 하려면 관리자 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_USER)
        void closeProduct_user_role() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(patch("/products/{productId}/close", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/close"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 상품을 판매 중지 할 수 없다")
        void closeProduct_unAuthorized() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(patch("/products/{productId}/close", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1/close"));
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProducts {
        @Test
        @DisplayName("상품 목록을 조회한다")
        void getProducts() throws Exception {
            //given
            ProductResult.Summary summary = mockSummaryResult();
            PageRequest pageable = PageRequest.of(0, 10);
            Page<ProductResult.Summary> results = new PageImpl<>(List.of(summary), pageable, 100L);
            PageDto<ProductResponse.Summary> response = PageDto.of(results, ProductResponse.Summary::from);
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("page", "1");
            paramMap.add("size", "10");
            paramMap.add("sort", "latest");
            paramMap.add("categoryId", "1");
            paramMap.add("name", "상품");
            paramMap.add("rating", "3");
            given(productService.getProducts(any(ProductCommand.Search.class)))
                    .willReturn(results);
            //when
            //then
            mockMvc.perform(get("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(paramMap))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @ParameterizedTest(name = "{0}")
        @DisplayName("상품 목록을 조회한다")
        @MethodSource("provideInvalidCondition")
        void getProducts_invalidCondition(String description, MultiValueMap<String, String> parameters, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(get("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .params(parameters))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products"));
        }

        private static Stream<Arguments> provideInvalidCondition() {
            return Stream.of(
                    Arguments.of("categoryId 가 0", invalidCondition("categoryId", "0"), "카테고리 Id는 0 또는 음수일 수 없습니다"),
                    Arguments.of("rating 이 음수", invalidCondition("rating", "-1"), "평점은 음수일 수 없습니다"),
                    Arguments.of("rating 이 5 이상", invalidCondition("rating", "6"), "최대 평점은 5점입니다")
            );
        }

        private static LinkedMultiValueMap<String, String> invalidCondition(String key, String value){
            LinkedMultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
            parameterMap.add(key, value);
            return parameterMap;
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
    }

    @Nested
    @DisplayName("상품 조회")
    class GetProductDetail {
        @Test
        @DisplayName("상품을 조회한다")
        void getProductDetail() throws Exception {
            //given
            ProductResult.Detail result = mockDetailResult();
            given(productService.getProduct(anyLong())).willReturn(result);
            ProductResponse.Detail response = ProductResponse.Detail.from(result);
            //when
            //then
            mockMvc.perform(get("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
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

    @Nested
    @DisplayName("상품 수정")
    class UpdateProduct {
        @Test
        @DisplayName("상품을 수정한다")
        @WithCustomMockUser
        void updateProduct() throws Exception {
            //given
            ProductRequest.Update request = fixtureMonkey.giveMeOne(ProductRequest.Update.class);
            ProductResult.Update result = fixtureMonkey.giveMeOne(ProductResult.Update.class);
            assert result != null;
            given(productService.updateProduct(any(ProductCommand.Update.class)))
                    .willReturn(result);
            ProductResponse.Update response = ProductResponse.Update.from(result);
            //when
            //then
            mockMvc.perform(put("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
        }

        @Test
        @DisplayName("상품을 수정하려면 관리자 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_USER)
        void updateProduct_user_role() throws Exception {
            //given
            ProductRequest.Update request = fixtureMonkey.giveMeOne(ProductRequest.Update.class);
            //when
            //then
            mockMvc.perform(put("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 상품을 수정할 수 없다")
        void updateProduct_unAuthorized() throws Exception {
            //given
            ProductRequest.Update request = fixtureMonkey.giveMeOne(ProductRequest.Update.class);
            //when
            //then
            mockMvc.perform(put("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideInvalidUpdateRequest")
        @WithCustomMockUser
        @DisplayName("상품 수정 요청 검증")
        void updateProduct_validation(String description, ProductRequest.Update request, String message) throws Exception {
            //given
            //when
            //then
            mockMvc.perform(put("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION"))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1"));
        }

        private static Stream<Arguments> provideInvalidUpdateRequest(){
            ProductRequest.Update VALID_BASE_UPDATE = ProductRequest.Update.builder()
                    .name("새 이름")
                    .categoryId(1L)
                    .description("상품 설명")
                    .build();
            return Stream.of(
                    Arguments.of("빈 이름", VALID_BASE_UPDATE.toBuilder().name(null).build(), "상품 이름은 필수 입니다"),
                    Arguments.of("카테고리 id 가 null", VALID_BASE_UPDATE.toBuilder().categoryId(null).build(), "카테고리 id는 필수 입니다")
            );
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class DeleteProduct {
        @Test
        @DisplayName("상품을 삭제한다")
        @WithCustomMockUser
        void deleteProduct() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(delete("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("상품을 삭제하려면 관리자 권한이여야 한다")
        @WithCustomMockUser(userRole = UserRole.ROLE_USER)
        void deleteProduct_user_role() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(delete("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("요청 권한이 부족합니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1"));
        }

        @Test
        @DisplayName("로그인 하지 않은 사용자는 상품을 삭제할 수 없다")
        void deleteProduct_unAuthorized() throws Exception {
            //given
            //when
            //then
            mockMvc.perform(delete("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("인증이 필요한 접근입니다"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.path").value("/products/1"));
        }
    }
}
