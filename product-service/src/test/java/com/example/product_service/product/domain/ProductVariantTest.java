package com.example.product_service.product.domain;

import com.example.product_service.common.domain.vo.Money;
import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.product.domain.context.AddVariantContext;
import com.example.product_service.product.domain.context.CreateProductContext;
import com.example.product_service.product.domain.vo.SalePrice;
import com.example.product_service.product.exception.ProductErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductVariantTest {

    @Test
    @DisplayName("상품 변형을 생성한다")
    void create() {
        //given
        Product product = aProduct();
        AddVariantContext context = aVariantContext(1L, "SKU-001", 100);
        //when
        ProductVariant variant = ProductVariant.create(context, product);
        //then
        assertThat(variant.getId()).isEqualTo(1L);
        assertThat(variant.getProduct()).isEqualTo(product);
        assertThat(variant.getStatus()).isEqualTo(ProductVariantStatus.PREPARING);
        assertThat(variant.getSku()).isEqualTo("SKU-001");
        assertThat(variant.getStock()).isEqualTo(100);
        assertThat(variant.getProductVariantOptionValues()).hasSize(1);
        assertThat(variant.getProductVariantOptionValues().get(0).getOptionValueId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("재고가 0보다 작으면 상품 변형을 생성할 수 없다")
    void create_whenStockIsNegative_thenThrownException() {
        //given
        Product product = aProduct();
        AddVariantContext context = aVariantContext(1L, "SKU-001", -1);
        //when
        //then
        assertThatThrownBy(() -> ProductVariant.create(context, product))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.VARIANT_INVALID_STOCK);
    }

    @Test
    @DisplayName("상품 변형을 판매 중지한다")
    void discontinued() {
        //given
        Product product = aProduct();
        ProductVariant variant = ProductVariant.create(aVariantContext(1L, "SKU-001", 100), product);
        LocalDateTime discontinuedAt = LocalDateTime.now();
        //when
        variant.discontinued(discontinuedAt);
        //then
        assertThat(variant.getStatus()).isEqualTo(ProductVariantStatus.DISCONTINUED);
        assertThat(variant.getDiscontinuedAt()).isEqualTo(discontinuedAt);
    }

    private Product aProduct() {
        return Product.create(
                CreateProductContext.builder()
                        .id(1L)
                        .categoryId(10L)
                        .name("상품")
                        .description("상품 설명")
                        .build()
        );
    }

    private AddVariantContext aVariantContext(Long id, String sku, int stock) {
        return AddVariantContext.builder()
                .id(id)
                .sku(sku)
                .salePrice(SalePrice.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L)))
                .stock(stock)
                .optionValues(List.of(
                        AddVariantContext.AddVariantOptionValueContext.builder()
                                .id(id)
                                .optionTypeId(10L)
                                .optionValueId(100L)
                                .build()
                ))
                .build();
    }
}
