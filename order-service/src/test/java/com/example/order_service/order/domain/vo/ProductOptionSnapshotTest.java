package com.example.order_service.order.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    @Test
    @DisplayName("상품 옵션 타입 이름이 누락되면 예외가 발생한다.")
    void of_optionTypeName_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductOptionSnapshot.of(null, "XL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 옵션 타입은 필수 입니다.");
    }

    @Test
    @DisplayName("상품 옵션 값 이름이 누락되면 예외가 발생한다.")
    void of_optionValueName_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductOptionSnapshot.of("사이즈", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 옵션 값은 필수 입니다.");
    }
}