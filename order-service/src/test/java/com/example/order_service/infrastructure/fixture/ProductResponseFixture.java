package com.example.order_service.infrastructure.fixture;

import com.example.order_service.infrastructure.dto.response.product.ProductResponse;

import java.util.List;

public class ProductResponseFixture {

    public static ProductResponse.ProductResponseBuilder anProductResponse() {
        return ProductResponse.builder()
                .products(List.of(anProductDetail().build()));
    }

    public static ProductResponse.ProductDetail.ProductDetailBuilder anProductDetail() {
        return ProductResponse.ProductDetail.builder()
                .productId(1L)
                .productVariantId(1L)
                .status("ON_SALE")
                .stock(100)
                .sku("SKU")
                .productName("상품")
                .thumbnail("/product/product.jpg")
                .unitPrice(anUnitPrice().build())
                .options(List.of(anOption().build()));
    }

    public static ProductResponse.UnitPrice.UnitPriceBuilder anUnitPrice() {
        return ProductResponse.UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L);
    }

    public static ProductResponse.ProductOption.ProductOptionBuilder anOption() {
        return ProductResponse.ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }
}
