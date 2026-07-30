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

    @Test
    @DisplayName("상품 식별자가 누락되면 예외가 발생한다.")
    void of_productId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(null, 1L, "sku",
                "productName", "/product/product"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 식별자는 필수 입니다.");
    }

    @Test
    @DisplayName("상품 변형 식별자가 누락되면 예외가 발생한다.")
    void of_productVariantId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(1L, null, "SKU", "상품", "/product/product.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 변형 식별자는 필수 입니다.");
    }

    @Test
    @DisplayName("상품 SKU가 누락되면 예외가 발생한다.")
    void of_sku_empty() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(1L, 1L, null,
                "productName", "/product/product"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 SKU는 필수 입니다.");
    }

    @Test
    @DisplayName("상품명이 누락되면 예외가 발생한다.")
    void of_productName_empty() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(1L, 1L, "SKU",
                null, "/product/product/product.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품명은 필수 입니다.");
    }

    @Test
    @DisplayName("상품 썹네일이 누락되면 예외가 발생한다.")
    void of_thumbnail_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ProductSnapshot.of(1L, 1L, "SKU",
                "productName", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 썸네일은 필수 입니다.");
    }

}