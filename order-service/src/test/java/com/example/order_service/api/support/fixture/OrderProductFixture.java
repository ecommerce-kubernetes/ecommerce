package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse.ProductOptionInfo;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse.UnitPrice;
import com.example.order_service.api.order.infrastructure.client.product.dto.ProductStatus;

import java.util.List;

public class OrderProductFixture {

    public static OrderProductResponse.OrderProductResponseBuilder anOrderProductResponse() {
        return OrderProductResponse.builder()
                .productId(1L)
                .productVariantId(2L)
                .status(ProductStatus.ON_SALE)
                .sku("TEST")
                .productName("상품")
                .thumbnailUrl("http://thumbnail.jpg")
                .unitPrice(anUnitPrice().build())
                .stockQuantity(100)
                .productOptionInfos(List.of(anProductOption().build()));
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
}
