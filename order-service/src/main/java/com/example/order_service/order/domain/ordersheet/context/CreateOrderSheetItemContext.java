package com.example.order_service.order.domain.ordersheet.context;

import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateOrderSheetItemContext(
        ProductSnapshot productSnapshot,
        ProductPriceSnapshot priceSnapshot,
        int quantity,
        List<ProductOptionSnapshot> optionSnapshots
) {
}
