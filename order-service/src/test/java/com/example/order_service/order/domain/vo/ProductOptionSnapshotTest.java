package com.example.order_service.order.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductOptionSnapshotTest {

    @Test
    @DisplayName("상품 옵션 스냅샷 생성한다")
    void of() {
        //given
        //when
        ProductOptionSnapshot option = ProductOptionSnapshot.of("사이즈", "XL");
        //then
        assertThat(option)
                .extracting("optionTypeName", "optionValueName")
                .containsExactlyInAnyOrder(
                        "사이즈", "XL"
                );
    }

    @ParameterizedTest(name = "옵션 타입이 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_optionType(String optionType) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductOptionSnapshot.of(optionType, "XL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 옵션 타입은 필수입니다");
    }

    @ParameterizedTest(name = "옵션 값이 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_optionValue(String optionValue) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductOptionSnapshot.of("사이즈", optionValue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 옵션 값은 필수입니다");
    }
}