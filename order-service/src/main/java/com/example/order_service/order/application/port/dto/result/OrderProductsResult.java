package com.example.order_service.order.application.port.dto.result;

import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public record OrderProductsResult(
        List<OrderProductDetail> products
) {
    public Map<Long, OrderProductDetail> getProductsMap() {
        return products.stream()
                .collect(Collectors.toMap(
                        item -> item.productSnapshot.getProductVariantId(),
                        Function.identity()
                ));
    }

    @Builder
    public record OrderProductDetail(
            ProductSnapshot productSnapshot,
            OrderProductStatus status,
            Integer stock,
            ProductPriceSnapshot priceSnapshot,
            List<ProductOptionSnapshot> options
    ) {
    }
}
