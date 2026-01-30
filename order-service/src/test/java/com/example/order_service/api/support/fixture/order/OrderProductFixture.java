package com.example.order_service.api.support.fixture.order;

import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo.ProductOption;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse.ProductOptionInfo;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse.UnitPrice;

import java.util.List;

public class OrderProductFixture {

    public static OrderProductResponse.OrderProductResponseBuilder anOrderProductResponse() {
        return OrderProductResponse.builder()
                .productId(1L)
                .productVariantId(2L)
                .status("ON_SALE")
                .sku("TEST")
                .productName("상품")
                .thumbnailUrl("http://thumbnail.jpg")
                .unitPrice(anUnitPrice().build())
                .stockQuantity(100)
                .itemOptions(List.of(anProductOption().build()));
    }

    public static UnitPrice.UnitPriceBuilder anUnitPrice() {
        return UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L);
    }

    public static ProductOptionInfo.ProductOptionInfoBuilder anProductOption() {
        return ProductOptionInfo.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }

    public static OrderProductInfo.OrderProductInfoBuilder anOrderProductInfo(){
        return OrderProductInfo.builder()
                .productId(1L)
                .productVariantId(1L)
                .sku("TEST")
                .productName("상품")
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .thumbnail("http://thumbnail.jpg")
                .productOption(List.of(anOrderProductOption().build()));
    }

    public static ProductOption.ProductOptionBuilder anOrderProductOption() {
        return ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }
}
