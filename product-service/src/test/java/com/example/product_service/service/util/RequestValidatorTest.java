package com.example.product_service.service.util;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.entity.*;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.service.dto.ProductCreationCommand;
import com.example.product_service.service.dto.ProductVariantCommand;
import com.example.product_service.service.dto.VariantOptionValueRef;
import com.example.product_service.service.util.validator.RequestValidator;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.example.product_service.common.MessagePath.*;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestValidatorTest {
    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    RequestValidator validator;

    @Test
    @DisplayName("productRequest 검증 테스트-성공")
    void validateProductRequestStructureTest_success(){
        ProductRequest request = createProductRequest();
        ProductCreationCommand productCreationCommand = validator.validateProductRequest(request);

        assertThat(productCreationCommand.getName()).isEqualTo("name");
        assertThat(productCreationCommand.getDescription()).isEqualTo("description");
        assertThat(productCreationCommand.getImageUrls())
                .containsExactlyInAnyOrder("http://test.jpg");

        assertThat(productCreationCommand.getOptionTypeCommands())
                .extracting("optionTypeId", "priority", "activate")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1, true)
                );

        assertThat(productCreationCommand.getVariantCommands())
                .extracting("sku", "price", "stockQuantity", "discountRate")
                .containsExactlyInAnyOrder(
                        tuple("sku", 1000, 100, 10)
                );

        assertThat(productCreationCommand.getVariantCommands())
                .flatExtracting(ProductVariantCommand::getVariantOptionValues)
                .extracting(VariantOptionValueRef::getOptionTypeId, VariantOptionValueRef::getOptionValueId)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L)
                );
    }

    @Test
    @DisplayName("ProductRequest 검증 테스트-실패(상품 옵션 타입 리스트에 같은 옵션 타입 아이디가 존재하는 경우)")
    void validateProductRequestStructureTest_duplicate_ProductOptionType_id(){
        ProductRequest request = createProductRequest();

        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 1),
                new ProductOptionTypeRequest(1L, 2))
        );
        mockMessageUtil(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST, "Cannot specify the same product option type");
        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST));
    }

    @Test
    @DisplayName("ProductRequest 검증 테스트-실패(상품 옵션 타입 리스트에 같은 우선순위가 존재할 경우)")
    void validateProductRequestStructureTest_duplicateProductOptionType_priority(){
        ProductRequest request = createProductRequest();

        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 1),
                new ProductOptionTypeRequest(2L, 1))
        );
        mockMessageUtil(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST, "Same option type priority cannot be specified");
        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_PRIORITY_BAD_REQUEST));
    }

    @Test
    @DisplayName("ProductRequest 검증 테스트-실패(상품 변형의 동일한 SKU 가 있는 경우)")
    void validateProductRequestStructureTest_duplicate_productVariantSku(){
        ProductRequest request = createProductRequest();
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("sku",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L, 1L)
                                )
                        ),
                        new ProductVariantRequest("sku",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L, 3L)
                                )
                        )
                )
        );

        mockMessageUtil(PRODUCT_VARIANT_SKU_CONFLICT, "Product Variant SKU Conflict");

        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_SKU_CONFLICT));
    }

    @Test
    @DisplayName("ProductRequest 검증 테스트-실패(상품 옵션 타입과 일치하지 않은 상품 변형 옵션 타입이 들어올 경우)")
    void validateProductRequestStructureTest_cardinality_violation(){
        mockMessageUtil(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION,
                "Each product variant must have exactly one option value per option type");
        ProductRequest request = createProductRequest();
        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 1),
                        new ProductOptionTypeRequest(2L, 2))
        );
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("sku",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L, 1L)
                                )
                        )
                )
        );
        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("sku",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 6L),
                                        new VariantOptionValueRequest(3L, 4L)
                                )
                        )
                )
        );

        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("sku",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(3L, 6L)
                                )
                        )
                )
        );

        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));

        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("sku",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 8L),
                                        new VariantOptionValueRequest(1L, 2L)
                                )
                        )
                )
        );

        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    @Test
    @DisplayName("ProductRequest 검증 테스트-실패(상품 변형의 옵션 값이 중복될 경우)")
    void validateProductRequestStructureTest_duplicate_optionValue(){
        mockMessageUtil(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT,
                "Cannot add product variants with the same OptionValue");
        ProductRequest request = createProductRequest();

        request.setProductOptionTypes(
                List.of(new ProductOptionTypeRequest(1L, 1),
                        new ProductOptionTypeRequest(2L, 2))
        );
        request.setProductVariants(
                List.of(
                        new ProductVariantRequest("sku1",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L, 1L),
                                        new VariantOptionValueRequest(2L, 1L)
                                )
                        ),
                        new ProductVariantRequest("sku2",
                                100,
                                100,
                                10,
                                List.of(
                                        new VariantOptionValueRequest(1L,1L),
                                        new VariantOptionValueRequest(2L,1L)
                                )
                        )
                )
        );

        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_OPTION_VALUE_CONFLICT));
    }

    @Test
    @DisplayName("상품 변형 검증 테스트-성공")
    void validateVariantRequestTest_success(){
        ProductVariantRequest request = createProductVariantRequest();
        ProductVariantCommand productVariantCommand = validator.validateVariantRequest(request);

        assertThat(productVariantCommand.getSku()).isEqualTo("sku");
        assertThat(productVariantCommand.getPrice()).isEqualTo(10000);
        assertThat(productVariantCommand.getStockQuantity()).isEqualTo(100);
        assertThat(productVariantCommand.getDiscountRate()).isEqualTo(10);
        assertThat(productVariantCommand.getVariantOptionValues())

                .extracting(VariantOptionValueRef::getOptionTypeId, VariantOptionValueRef::getOptionValueId)
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L)
                );
    }

    @Test
    @DisplayName("상품 변형 검증 테스트-실패(같은 옵션 타입 Id가 중복요청시)")
    void validateVariantRequestTest_duplicateOptionTypeId(){
        ProductVariantRequest request = createProductVariantRequest();
        request.setVariantOption(List.of(new VariantOptionValueRequest(1L,1L),
                new VariantOptionValueRequest(1L, 3L)));
        mockMessageUtil(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION, "Each product variant must have exactly one option value per option type");
        assertThatThrownBy(() -> validator.validateVariantRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_VALUE_CARDINALITY_VIOLATION));
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }

    private ProductRequest createProductRequest(){
        return new ProductRequest(
                "name",
                "description",
                1L,
                List.of("http://test.jpg"),
                List.of(new ProductOptionTypeRequest(1L, 1)),
                List.of(new ProductVariantRequest("sku", 1000, 100, 10,
                        List.of(
                                new VariantOptionValueRequest(1L, 1L)
                        )))

                );
    }

    private ProductVariantRequest createProductVariantRequest(){
        return new ProductVariantRequest(
                "sku",
                10000,
                100,
                10,
                List.of(new VariantOptionValueRequest(1L, 1L))
        );
    }
}