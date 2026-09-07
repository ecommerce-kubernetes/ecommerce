package com.example.product_service.product.fixture;

import com.example.product_service.product.adapter.in.web.dto.request.*;

import java.util.List;

public class ProductRequestFixture {

    public static CreateProductRequest.CreateProductRequestBuilder anCreateProductRequest() {
        return CreateProductRequest.builder()
                .name("상품")
                .categoryId(1L)
                .description("상품 설명");
    }

    public static UpdateProductRequest.UpdateProductRequestBuilder anUpdateProductRequest() {
        return UpdateProductRequest.builder()
                .name("새 상품")
                .categoryId(1L)
                .description("상품 설명");
    }

    public static RegisterProductOptionRequest.RegisterProductOptionRequestBuilder anRegisterProductOptionRequest() {
        return RegisterProductOptionRequest.builder()
                .optionTypeIds(List.of(1L, 2L));
    }

    public static ProductVariantDetailRequest.ProductVariantDetailRequestBuilder anProductVariantDetailRequest() {
        return ProductVariantDetailRequest.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .stockQuantity(100)
                .optionValueIds(List.of(1L, 2L));
    }

    public static AddProductVariantRequest.AddProductVariantRequestBuilder anAddProductVariantRequest() {
        return AddProductVariantRequest.builder()
                .variants(List.of(anProductVariantDetailRequest().build()));
    }

    public static AddProductImageRequest.AddProductImageRequestBuilder anAddProductImageRequest() {
        return AddProductImageRequest.builder()
                .images(List.of("/test/image.jpg"));
    }

    public static AddProductDescriptionImageRequest.AddProductDescriptionImageRequestBuilder anAddProductDescriptionImageRequest() {
        return AddProductDescriptionImageRequest.builder()
                .images(List.of("/test/description.jpg"));
    }
}
