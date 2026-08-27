package com.example.order_service.order.domain.ordersheet.context;

import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreateOrderSheetContext(
        Orderer orderer,
        ShippingAddress shippingAddress,
        List<CreateOrderSheetItemContext> items,
        LocalDateTime expiresAt
) {

    public CreateOrderSheetContext {
        Assert.notNull(orderer, "주문서(OrderSheet) 생성시 주문자는 필수이다.");
        Assert.notNull(items, "주문서(OrderSheet) 생성시 주문 항목은 필수이다.");
        Assert.notNull(expiresAt, "주문서(OrderSheet) 생성시 만료 시간은 필수이다.");
    }
}
