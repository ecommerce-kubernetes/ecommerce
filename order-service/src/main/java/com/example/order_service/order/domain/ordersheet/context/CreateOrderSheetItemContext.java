package com.example.order_service.order.domain.ordersheet.context;

import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import lombok.Builder;
import org.springframework.util.Assert;

import java.util.List;

@Builder
public record CreateOrderSheetItemContext(
        ProductSnapshot productSnapshot,
        ProductPriceSnapshot priceSnapshot,
        int quantity,
        List<ProductOptionSnapshot> optionSnapshots
) {

    public CreateOrderSheetItemContext {
        Assert.notNull(productSnapshot, "주문 항목(OrderSheetItem) 생성시 상품 정보는 필수이다.");
        Assert.notNull(priceSnapshot, "주문 항목(OrderSheetItem) 생성시 상품 가격은 필수이다.");
        Assert.notNull(optionSnapshots, "주문 항목(OrderSheetItem) 생성시 상품 옵션은 필수이다.");
    }
}
