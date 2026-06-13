package com.example.order_service.order.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSnapshotTest {

    @Test
    @DisplayName("상품 스냅샷을 생성한다")
    void of() {
        //given
        //when
        ProductSnapshot snapshot = ProductSnapshot.of(1L, 1L, "sku",
                "productName", "/product/product");
        //then
        assertThat(snapshot)
                .extracting("productId", "productVariantId", "sku", "productName", "thumbnail")
                .containsExactlyInAnyOrder(1L, 1L, "sku", "productName", "/product/product");
    }

    @ParameterizedTest(name = "상품 식별자가 null이면 예외가 발생한다")
    @CsvSource(
            value = {"null, 1", "1, null"},
            nullValues = "null"
    )
    void of_id_null(Long productId, Long productVariantId) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(productId, productVariantId, "sku",
                "productName", "/product/product"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 식별자는 필수 입니다");
    }

    @ParameterizedTest(name = "상품 SKU가 유효하지 않으면 이면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_sku_invalid(String sku) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(1L, 1L, sku,
                "productName", "/product/product"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 SKU 는 필수입니다");
    }

    @ParameterizedTest(name = "상품명이 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_productName_invalid(String productName) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(1L, 1L, "SKU",
                productName, "/product/product/product.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품명은 필수입니다");
    }

    @ParameterizedTest(name = "썸네일이 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_thumbnail_invalid(String thumbnail) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(1L, 1L, "SKU",
                "productName", thumbnail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 썸네일은 필수입니다");
    }

}