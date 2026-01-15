package com.example.product_service.support.fixture;

import com.example.product_service.api.product.controller.dto.*;
import com.example.product_service.api.product.service.dto.result.*;

import java.time.LocalDateTime;
import java.util.List;

public class ProductControllerFixture {
    public static final Long PRODUCT_ID = 1L;
    public static final Long VARIANT_ID_1 = 1L;
    public static final Long PRICE = 3000L;
    public static final Integer DISCOUNT_RATE = 10;

    public static ProductUpdateRequest.ProductUpdateRequestBuilder mockUpdateRequest() {
        return ProductUpdateRequest.builder()
                .name("새상품")
                .description("상품 설명")
                .categoryId(1L);
    }

    public static ProductUpdateResponse.ProductUpdateResponseBuilder mockUpdateResponse() {
        return ProductUpdateResponse.builder()
                .productId(PRODUCT_ID)
                .name("새상품")
                .description("상품 설명")
                .categoryId(1L);
    }

    public static ProductCreateRequest.ProductCreateRequestBuilder mockCreateRequest(){
        return ProductCreateRequest.builder()
                .name("상품")
                .categoryId(1L)
                .description("상품 설명");
    }

    public static ProductCreateResponse.ProductCreateResponseBuilder mockCreateResponse(){
        return ProductCreateResponse.builder()
                .productId(PRODUCT_ID);
    }

    public static ProductOptionSpecRequest.ProductOptionSpecRequestBuilder mockOptionSpecRequest(){
        return ProductOptionSpecRequest.builder()
                .optionTypeIds(
                        List.of(1L, 2L)
                );
    }

    public static VariantCreateRequest.VariantCreateRequestBuilder mockCreateVariantRequest() {
        return VariantCreateRequest.builder()
                .variants(
                        List.of(
                                VariantCreateRequest.VariantRequest.builder()
                                        .price(PRICE)
                                        .discountRate(DISCOUNT_RATE)
                                        .stockQuantity(100)
                                        .optionValueIds(List.of(1L, 2L))
                                        .build()
                        )
                );
    }

    public static VariantCreateResponse.VariantCreateResponseBuilder mockCreateVariantResponse() {
        return VariantCreateResponse.builder()
                .productId(PRODUCT_ID)
                .variants(
                        List.of(
                                VariantResponse.builder()
                                        .variantId(VARIANT_ID_1)
                                        .sku("PROD-XL")
                                        .optionValueIds(List.of(1L, 2L))
                                        .originalPrice(PRICE)
                                        .discountedPrice(2700L)
                                        .discountRate(DISCOUNT_RATE)
                                        .stockQuantity(100)
                                        .build()
                        )
                );
    }

    public static ProductImageCreateRequest.ProductImageCreateRequestBuilder mockImageRequest(){
        return ProductImageCreateRequest.builder()
                .images(List.of("http://image1.jpg", "http://image2.jpg"));
    }

    public static ProductOptionSpecResponse.ProductOptionSpecResponseBuilder mockOptionSpecResponse() {
        return ProductOptionSpecResponse.builder()
                .productId(PRODUCT_ID)
                .options(
                        List.of(
                                ProductOptionSpecResponse.OptionSpec.builder()
                                        .productOptionSpecId(1L)
                                        .optionTypeId(1L)
                                        .name("사이즈")
                                        .priority(1)
                                        .build()
                        )
                );
    }

    public static ProductImageCreateResponse.ProductImageCreateResponseBuilder mockImageResponse() {
        return ProductImageCreateResponse
                .builder()
                .productId(PRODUCT_ID)
                .images(
                        List.of(
                                ProductImageResponse.builder()
                                        .productImageId(1L)
                                        .imageUrl("http://image1.jpg")
                                        .order(1)
                                        .isThumbnail(true)
                                        .build()
                        ));
    }

    public static ProductStatusResponse.ProductStatusResponseBuilder mockStatusResponse() {
        return ProductStatusResponse.builder()
                .productId(PRODUCT_ID)
                .status("ON_SALE")
                .changedAt(LocalDateTime.now().toString());
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
                                ProductImageResponse.builder()
                                        .productImageId(1L)
                                        .imageUrl("http://image.jpg")
                                        .order(1)
                                        .isThumbnail(true)
                                        .build()))
                .variants(
                        List.of(
                                VariantResponse.builder()
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
