package com.example.product_service.service.util;

import com.example.product_service.common.MessageSourceUtil;
import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.product.ProductRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import com.example.product_service.dto.request.variant.VariantOptionValueRequest;
import com.example.product_service.exception.BadRequestException;
import com.example.product_service.repository.ProductVariantsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.product_service.common.MessagePath.PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST;
import static com.example.product_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductRequestValidatorTest {
    @Mock
    ProductVariantsRepository productVariantsRepository;

    @Mock
    MessageSourceUtil ms;

    @InjectMocks
    ProductRequestValidator validator;

    @Test
    @DisplayName("ProductRequest 검증 테스트-실패(상품 옵션 타입 리스트에 같은 옵션 타입 아이디가 존재하는 경우)")
    void validateProductRequestStructureTest_success(){
        ProductRequest request = new ProductRequest(
                "name",
                "description",
                1L,
                List.of(new ImageRequest("http://test.jpg", 0)),
                List.of(new ProductOptionTypeRequest(1L, 1),
                        new ProductOptionTypeRequest(1L, 2)),
                List.of(new ProductVariantRequest("sku", 3000, 100, 10, List.of(
                        new VariantOptionValueRequest(1L, 5L),
                        new VariantOptionValueRequest(2L, 7L)
                )))
        );
        mockMessageUtil(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST, "Cannot specify the same product option type");
        assertThatThrownBy(() -> validator.validateProductRequest(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(getMessage(PRODUCT_OPTION_TYPE_TYPE_BAD_REQUEST));
    }

    private void mockMessageUtil(String code, String returnMessage){
        when(ms.getMessage(code)).thenReturn(returnMessage);
    }
}