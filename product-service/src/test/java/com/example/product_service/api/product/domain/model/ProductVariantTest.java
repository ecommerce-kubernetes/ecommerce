package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductVariantTest {

    private OptionValue createOptionValue(Long id, String name) {
        OptionValue optionValue = OptionValue.create(name);
        ReflectionTestUtils.setField(optionValue, "id", id);
        return optionValue;
    }

    @Nested
    @DisplayName("상품 변형 옵션 추가")
    class AddOption {

        @Test
        @DisplayName("동일한 옵션의 상품 변형은 생성할 수 없다")
        void addProductVariantOptions_duplicate_option_value(){
            //given
            OptionValue xl = createOptionValue(1L, "XL");
            ProductVariant variant = ProductVariant.create("TEST", 10000L, 100, 10);
            //when
            //then
            assertThatThrownBy(() -> variant.addProductVariantOptions(List.of(xl, xl)))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ProductErrorCode.PRODUCT_VARIANT_DUPLICATE_OPTION);
        }
    }
}
