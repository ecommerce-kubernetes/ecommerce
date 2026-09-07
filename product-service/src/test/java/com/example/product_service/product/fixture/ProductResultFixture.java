package com.example.product_service.product.fixture;

import com.example.product_service.product.application.service.dto.result.ProductDetailResult;
import com.example.product_service.product.application.service.dto.result.ProductSummaryResult;
import com.example.product_service.product.domain.model.ProductStatus;

import java.util.List;

public class ProductResultFixture {

    public static ProductSummaryResult.ProductSummaryResultBuilder aProductSummaryResult() {
        return ProductSummaryResult.builder()
                .productId(1L)
                .name("베이직 코튼 티셔츠")
                .thumbnail("/products/thumb.jpg")
                .originalPrice(29000L)
                .discountRate(30)
                .price(20300L)
                .reviewCount(128L);
    }

    public static ProductDetailResult.ProductDetailResultBuilder aProductDetailResult() {
        ProductDetailResult.CategoryInfo category = ProductDetailResult.CategoryInfo.builder()
                .id(45L)
                .name("티셔츠")
                .build();

        ProductDetailResult.OptionValueDetail red = ProductDetailResult.OptionValueDetail.builder()
                .optionValueId(10L)
                .name("레드")
                .build();
        ProductDetailResult.OptionValueDetail blue = ProductDetailResult.OptionValueDetail.builder()
                .optionValueId(11L)
                .name("블루")
                .build();
        ProductDetailResult.OptionGroup optionGroup = ProductDetailResult.OptionGroup.builder()
                .optionTypeId(3L)
                .optionTypeName("색상")
                .values(List.of(red, blue))
                .build();

        ProductDetailResult.VariantDetail variant = ProductDetailResult.VariantDetail.builder()
                .variantId(501L)
                .sku("PROD123-레드-M")
                .optionValueIds(List.of(10L, 20L))
                .originalPrice(29000L)
                .discountRate(10)
                .price(26100L)
                .stockQuantity(15)
                .build();

        return ProductDetailResult.builder()
                .productId(123L)
                .name("베이직 코튼 티셔츠")
                .description("설명 텍스트...")
                .status(ProductStatus.ON_SALE)
                .category(category)
                .images(List.of("/products/1.jpg", "/products/2.jpg"))
                .descriptionImages(List.of("/products/d1.jpg", "/products/d2.jpg"))
                .rating(4.5)
                .reviewCount(128L)
                .originalPrice(29000L)
                .discountRate(10)
                .price(26100L)
                .options(List.of(optionGroup))
                .variants(List.of(variant));
    }
}
