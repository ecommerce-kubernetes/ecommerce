package com.example.order_service.order.application.service.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.port.dto.OrderProductStatus;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;

import java.util.List;

public class OrderProductResultFixture {

    public static OrderProductsResult.OrderProductsResultBuilder anOrderProducts() {
        OrderProductsResult.OrderProductDetail product = anOrderProduct().build();
        return OrderProductsResult.builder()
                .products(List.of(product));
    }

    public static OrderProductsResult.OrderProductDetail.OrderProductDetailBuilder anOrderProduct() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot optionSnapshot = ProductOptionSnapshot.of("사이즈", "XL");
        return OrderProductsResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .status(OrderProductStatus.ON_SALE)
                .stock(100)
                .priceSnapshot(priceSnapshot)
                .options(List.of(optionSnapshot));
    }
}
