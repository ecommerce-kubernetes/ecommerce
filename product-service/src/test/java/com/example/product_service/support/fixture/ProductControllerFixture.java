package com.example.product_service.support.fixture;

import com.example.product_service.api.product.service.dto.result.*;

import java.time.LocalDateTime;
import java.util.List;

public class ProductControllerFixture {
    public static final Long PRODUCT_ID = 1L;
    public static final Long VARIANT_ID_1 = 1L;
    public static final Long PRICE = 3000L;
    public static final Integer DISCOUNT_RATE = 10;

    public static ProductUpdateResponse.ProductUpdateResponseBuilder mockUpdateResponse() {
        return ProductUpdateResponse.builder()
                .productId(PRODUCT_ID)
                .name("새상품")
                .description("상품 설명")
                .categoryId(1L);
    }

    public static ProductSummaryResponse.ProductSummaryResponseBuilder mockSummaryResponse() {
        return ProductSummaryResponse.builder()
                .productId(PRODUCT_ID)
                .name("상품")
                .thumbnail("http://image.jpg")
                .displayPrice(2700L)
                .originalPrice(PRICE)
                .maxDiscountRate(DISCOUNT_RATE)
                .categoryId(1L)
                .publishedAt(LocalDateTime.now().toString())
                .rating(3D)
                .reviewCount(100L)
                .status("ON_SALE");
    }

    public static ProductDetailResponse.ProductDetailResponseBuilder mockDetailResponse() {
        return ProductDetailResponse.builder()
                .productId(PRODUCT_ID)
                .name("상품")
                .status("ON_SALE")
                .categoryId(1L)
                .displayPrice(2700L)
                .originalPrice(PRICE)
                .maxDiscountRate(DISCOUNT_RATE)
                .rating(3D)
                .reviewCount(100L)
                .optionGroups(
                        List.of(
                                ProductDetailResponse.OptionGroup.builder()
                                        .optionTypeId(1L)
                                        .name("사이즈")
                                        .values(
                                                List.of(
                                                        ProductDetailResponse.OptionValueResponse.builder()
                                                                .optionValueId(1L)
                                                                .name("XL").build()
                                                )
                                        )
                                        .build()))
                .images(
                        List.of(
                                ProductImageResult.builder()
                                        .imagePath("http://image.jpg")
                                        .sortOrder(1)
                                        .isThumbnail(true)
                                        .build()))
                .descriptionImages(
                        List.of(
                                ProductDescriptionImageResponse.builder()
                                        .imagePath("http://description.jpg")
                                        .sortOrder(1)
                                        .build()
                        )
                )
                .variants(
                        List.of(
                                VariantResult.builder()
                                        .variantId(VARIANT_ID_1)
                                        .sku("PROD-XL")
                                        .optionValueIds(List.of(1L))
                                        .originalPrice(PRICE)
                                        .discountedPrice(2700L)
                                        .discountRate(DISCOUNT_RATE)
                                        .stockQuantity(100).build()
                        )
                );
    }
}
