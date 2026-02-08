package com.example.order_service.api.support.fixture.cart;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo.ProductOption;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse.ProductOptionInfo;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse.UnitPrice;

import java.util.List;

public class CartProductFixture {

    public static CartProductResponse.CartProductResponseBuilder anCartProductResponse() {
        return CartProductResponse.builder()
                .productId(1L)
                .productVariantId(1L)
                .status("ON_SALE")
                .productName("상품")
                .unitPrice(anCartProductUnitPrice().build())
                .thumbnailUrl("http://thumbnail.jpg")
                .itemOptions(List.of(anCartProductOptionInfo().build()));
    }

    public static UnitPrice.UnitPriceBuilder anCartProductUnitPrice() {
        return UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L);
    }

    public static ProductOptionInfo.ProductOptionInfoBuilder anCartProductOptionInfo() {
        return ProductOptionInfo.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }

    public static CartProductInfo.CartProductInfoBuilder anCartProductInfo() {
        return CartProductInfo.builder()
                .productId(1L)
                .productVariantId(1L)
                .status(ProductStatus.ON_SALE)
                .productName("상품")
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .thumbnail("http://thumbnail.jpg")
                .productOption(List.of(anProductOption().build()));
    }

    public static ProductOption.ProductOptionBuilder anProductOption() {
        return ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }
}
