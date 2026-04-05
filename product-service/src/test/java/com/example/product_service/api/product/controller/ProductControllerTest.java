package com.example.product_service.api.product.controller;

import com.example.product_service.api.common.dto.PageDto;
import com.example.product_service.api.common.security.model.UserRole;
import com.example.product_service.api.product.controller.dto.ProductSearchCondition;
import com.example.product_service.api.product.controller.dto.request.ProductRequest.*;
import com.example.product_service.api.product.controller.dto.response.ProductResponse.*;
import com.example.product_service.api.product.service.dto.command.ProductCreateCommand;
import com.example.product_service.api.product.service.dto.command.ProductUpdateCommand;
import com.example.product_service.api.product.service.dto.command.ProductVariantsCreateCommand;
import com.example.product_service.api.product.service.dto.result.*;
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
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Stream;

import static com.example.product_service.support.fixture.ProductControllerFixture.mockDetailResponse;
import static com.example.product_service.support.fixture.ProductControllerFixture.mockSummaryResponse;
import static org.mockito.ArgumentMatchers.*;
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
            CreateRequest request = fixtureMonkey.giveMeOne(CreateRequest.class);
            ProductCreateResult result = fixtureMonkey.giveMeOne(ProductCreateResult.class);
            assert result != null;
            given(productService.createProduct(any(ProductCreateCommand.class)))
                    .willReturn(result);
            CreateResponse response = CreateResponse.from(result);
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
            CreateRequest request = fixtureMonkey.giveMeOne(CreateRequest.class);
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
            CreateRequest request = fixtureMonkey.giveMeOne(CreateRequest.class);
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
        void createProduct_Validation(String description, CreateRequest request, String message) throws Exception {
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
                    Arguments.of("상품 이름이 공백", CreateRequest.builder()
                                    .name(null).categoryId(1L).description("상품 설명").build(),
                            "상품 이름은 필수 입니다"),
                    Arguments.of("카테고리 Id가 null", CreateRequest.builder()
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
            OptionRegisterRequest request = fixtureMonkey.giveMeOne(OptionRegisterRequest.class);
            ProductOptionResponse result = fixtureMonkey.giveMeOne(ProductOptionResponse.class);
            assert result != null;
            OptionRegisterResponse response = OptionRegisterResponse.from(result);
            given(productService.defineOptions(anyLong(), anyList()))
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
            OptionRegisterRequest request = fixtureMonkey.giveMeOne(OptionRegisterRequest.class);
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
            OptionRegisterRequest request = fixtureMonkey.giveMeOne(OptionRegisterRequest.class);
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
        void registerProductOption_invalidRequest(String description, OptionRegisterRequest request, String message) throws Exception {
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
            ProductOptionRequest VALID_BASE_OPTION = ProductOptionRequest.builder()
                    .optionTypeId(1L)
                    .priority(1)
                    .build();
            return Stream.of(
                    //필수 필드 누락
                    Arguments.of(
                            "옵션 리스트가 null",
                            wrap(null),
                            "옵션 리스트는 필수 입니다"
                    ),
                    Arguments.of(
                            "옵션 타입 아이디가 null",
                            wrap(List.of(
                                    VALID_BASE_OPTION.toBuilder().optionTypeId(null).build()
                            )),
                            "옵션 타입 Id는 필수 입니다"
                    ),
                    Arguments.of(
                            "옵션 타입 우선순위가 null",
                            wrap(List.of(
                                    VALID_BASE_OPTION.toBuilder().priority(null).build()
                            )),
                            "옵션 우선순위는 필수 입니다"
                    ),
                    //허용 범위 이탈
                    Arguments.of(
                            "옵션 타입 우선순위가 1미만",
                            wrap(List.of(
                                    VALID_BASE_OPTION.toBuilder().priority(0).build()
                            )),
                            "옵션 우선순위는 1이상 이여야 합니다"
                    ),
                    //요청 내부 중복 옵션
                    Arguments.of(
                            "중복된 옵션 타입 Id",
                            wrap(List.of(
                                    VALID_BASE_OPTION.toBuilder().optionTypeId(1L).build(),
                                    VALID_BASE_OPTION.toBuilder().optionTypeId(1L).build()
                            )),
                            "중복된 옵션 종류(optionTypeId)가 포함되어 있습니다"
                    ),
                    //최대 옵션 개수 초과
                    Arguments.of(
                            "옵션 개수가 3개 이상",
                            wrap(List.of(
                                    VALID_BASE_OPTION.toBuilder().optionTypeId(1L).priority(1).build(),
                                    VALID_BASE_OPTION.toBuilder().optionTypeId(2L).priority(2).build(),
                                    VALID_BASE_OPTION.toBuilder().optionTypeId(3L).priority(3).build(),
                                    VALID_BASE_OPTION.toBuilder().optionTypeId(4L).priority(4).build()
                            )),
                            "옵션은 최대 3개까지만 설정 가능합니다"
                    )
            );
        }

        private static OptionRegisterRequest wrap(List<ProductOptionRequest> options) {
            return OptionRegisterRequest.builder()
                    .options(options)
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
            AddVariantRequest request = fixtureMonkey.giveMeOne(AddVariantRequest.class);
            AddVariantResult result = fixtureMonkey.giveMeOne(AddVariantResult.class);
            assert result != null;
            AddVariantResponse response = AddVariantResponse.from(result);
            given(productService.createVariants(any(ProductVariantsCreateCommand.class)))
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
            AddVariantRequest request = fixtureMonkey.giveMeOne(AddVariantRequest.class);
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
            AddVariantRequest request = fixtureMonkey.giveMeOne(AddVariantRequest.class);
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
        void createVariants_validation(String description, AddVariantRequest request, String message) throws Exception {
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
            VariantRequest VALID_BASE_VARIANT =
                    VariantRequest.builder()
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

        private static AddVariantRequest wrap(VariantRequest variantRequest) {
            return AddVariantRequest.builder()
                    .variants(variantRequest == null ? null : List.of(variantRequest))
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
            AddImageRequest request = fixtureMonkey.giveMeOne(AddImageRequest.class);
            ProductImageCreateResult result = fixtureMonkey.giveMeOne(ProductImageCreateResult.class);
            assert result != null;
            AddImageResponse response = AddImageResponse.from(result);
            given(productService.updateImages(anyLong(), anyList()))
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
            AddImageRequest request = fixtureMonkey.giveMeOne(AddImageRequest.class);
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
            AddImageRequest request = fixtureMonkey.giveMeOne(AddImageRequest.class);
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
        void addImage_invalidRequest(String description, AddImageRequest request, String message) throws Exception {
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
            ImageRequest VALID_BASE_IMAGE = ImageRequest.builder()
                    .imagePath("/test/image.jpg")
                    .isThumbnail(true)
                    .sortOrder(1)
                    .build();
            return Stream.of(
                    Arguments.of(
                            "이미지 리스트가 null",
                            wrap(null),
                            "최소 1장의 이미지를 등록해야 합니다"
                    ),
                    Arguments.of(
                            "이미지 Path가 null",
                            wrap(VALID_BASE_IMAGE.toBuilder().imagePath(null).build()),
                            "이미지 경로는 필수 입니다"
                    ),
                    Arguments.of(
                            "잘못된 형식의 path",
                            wrap(VALID_BASE_IMAGE.toBuilder().imagePath("invalidPath").build()),
                            "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
                    ),
                    Arguments.of(
                            "썸네일 여부가 null",
                            wrap(VALID_BASE_IMAGE.toBuilder().isThumbnail(null).build()),
                            "썸네일 여부는 필수 입니다"
                    ),
                    Arguments.of(
                            "정렬 순서가 null",
                            wrap(VALID_BASE_IMAGE.toBuilder().sortOrder(null).build()),
                            "정렬 순서는 필수 입니다"
                    ),
                    Arguments.of(
                            "정렬 순서가 1미만",
                            wrap(VALID_BASE_IMAGE.toBuilder().sortOrder(0).build()),
                            "정렬 순서는 1 이상이여야 합니다"
                    )
            );
        }

        private static AddImageRequest wrap(ImageRequest imageRequest) {
            return AddImageRequest.builder()
                    .images(imageRequest == null ? null : List.of(imageRequest))
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
            AddDescriptionImageRequest request = fixtureMonkey.giveMeOne(AddDescriptionImageRequest.class);
            ProductDescriptionImageResult result = fixtureMonkey.giveMeOne(ProductDescriptionImageResult.class);
            assert result != null;
            given(productService.updateDescriptionImages(anyLong(), anyList()))
                    .willReturn(result);
            AddDescriptionImageResponse response = AddDescriptionImageResponse.from(result);
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
            AddDescriptionImageRequest request = fixtureMonkey.giveMeOne(AddDescriptionImageRequest.class);
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
            AddDescriptionImageRequest request = fixtureMonkey.giveMeOne(AddDescriptionImageRequest.class);
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
        void updateDescriptionImage_Validation(String description, AddDescriptionImageRequest request, String message) throws Exception {
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
            DescriptionImageRequest VALID_BASE_IMAGE = DescriptionImageRequest.builder()
                    .imagePath("/test/image.jpg")
                    .sortOrder(1)
                    .build();
            return Stream.of(
                    Arguments.of(
                            "images 가 null",
                            wrap(null),
                            "최소 1장의 이미지를 등록해야 합니다"
                    ),
                    Arguments.of(
                            "imagePath가 null",
                            wrap(VALID_BASE_IMAGE.toBuilder().imagePath(null).build()),
                            "이미지 경로는 필수 입니다"
                    ),
                    Arguments.of(
                            "잘못된 형식의 path",
                            wrap(VALID_BASE_IMAGE.toBuilder().imagePath("invalidPath").build()),
                            "이미지 경로는 '/'로 시작하는 유효한 이미지 파일이어야 합니다"
                    ),
                    Arguments.of(
                            "정렬 순서가 null",
                            wrap(VALID_BASE_IMAGE.toBuilder().sortOrder(null).build()),
                            "정렬 순서는 필수 입니다"
                    ),
                    Arguments.of(
                            "정렬 순서가 1미만",
                            wrap(VALID_BASE_IMAGE.toBuilder().sortOrder(0).build()),
                            "정렬 순서는 1 이상이여야 합니다"
                    )
            );
        }

        private static AddDescriptionImageRequest wrap(DescriptionImageRequest request) {
            return AddDescriptionImageRequest.builder()
                    .images(request == null ? null : List.of(request))
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
            ProductStatusResult result = fixtureMonkey.giveMeOne(ProductStatusResult.class);
            assert result != null;
            given(productService.publish(anyLong()))
                    .willReturn(result);
            PublishResponse response = PublishResponse.from(result);
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
            ProductStatusResult result = fixtureMonkey.giveMeOne(ProductStatusResult.class);
            assert result != null;
            given(productService.closedProduct(anyLong()))
                    .willReturn(result);
            CloseResponse response = CloseResponse.from(result);
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
            ProductSummaryResponse summary = mockSummaryResponse().build();
            PageDto<ProductSummaryResponse> response = PageDto.<ProductSummaryResponse>builder().content(List.of(summary))
                    .currentPage(1)
                    .totalPage(10)
                    .pageSize(10)
                    .totalElement(100)
                    .build();

            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add("page", "1");
            paramMap.add("size", "10");
            paramMap.add("sort", "latest");
            paramMap.add("categoryId", "1");
            paramMap.add("name", "상품");
            paramMap.add("rating", "3");
            given(productService.getProducts(any(ProductSearchCondition.class)))
                    .willReturn(response);
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
    }

    @Nested
    @DisplayName("상품 조회")
    class GetProductDetail {
        @Test
        @DisplayName("상품을 조회한다")
        void getProductDetail() throws Exception {
            //given
            ProductDetailResponse response = mockDetailResponse().build();
            given(productService.getProduct(anyLong())).willReturn(response);
            //when
            //then
            mockMvc.perform(get("/products/{productId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(objectMapper.writeValueAsString(response)));
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
            UpdateRequest request = fixtureMonkey.giveMeOne(UpdateRequest.class);
            ProductUpdateResponse result = fixtureMonkey.giveMeOne(ProductUpdateResponse.class);
            assert result != null;
            given(productService.updateProduct(any(ProductUpdateCommand.class)))
                    .willReturn(result);
            UpdateResponse response = UpdateResponse.from(result);
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
            UpdateRequest request = fixtureMonkey.giveMeOne(UpdateRequest.class);
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
            UpdateRequest request = fixtureMonkey.giveMeOne(UpdateRequest.class);
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
        void updateProduct_validation(String description, UpdateRequest request, String message) throws Exception {
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
            UpdateRequest VALID_BASE_UPDATE = UpdateRequest.builder()
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
