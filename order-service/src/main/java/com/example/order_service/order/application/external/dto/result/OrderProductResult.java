package com.example.order_service.order.application.external.dto.result;

import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderProductResult {

    @Builder
    public record ProductList(
            List<Info> products
    ) {
        public Map<Long, OrderProductResult.Info> getProductsMap() {
            return products.stream()
                    .collect(Collectors.toMap(item -> item.productSnapshot.getProductVariantId(), Function.identity()));
        }
    }

    @Builder
    public record Info(
            ProductSnapshot productSnapshot,
            ProductPriceSnapshot priceSnapshot,
            List<ProductOptionSnapshot> options
    ) {
    }
}
