package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.order.application.port.dto.OrderCartItemsResult;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OrderSheetContextFactory {

    public CreateOrderSheetContext createForCart(OrdererProfileResult ordererProfileResult,
                                                 OrderCartItemsResult cartItems,
                                                 OrderProductsResult products,
                                                 LocalDateTime expiresAt) {
        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        List<CreateOrderSheetItemContext> items = cartItems.items().stream()
                .map(item -> createItemContext(item.productVariantId(), item.quantity(), productsMap))
                .toList();

        return buildContext(ordererProfileResult, items, expiresAt);
    }

    public CreateOrderSheetContext createForDirect(OrdererProfileResult ordererProfileResult,
                                                   CreateDirectOrderSheetCommand command,
                                                   OrderProductsResult products,
                                                   LocalDateTime expiresAt) {
        Map<Long, OrderProductsResult.OrderProductDetail> productsMap = products.getProductsMap();

        List<CreateOrderSheetItemContext> items = command.items().stream()
                .map(item -> createItemContext(item.productVariantId(), item.quantity(), productsMap))
                .toList();
        return buildContext(ordererProfileResult, items, expiresAt);
    }

    private CreateOrderSheetItemContext createItemContext(Long variantId, int quantity, Map<Long, OrderProductsResult.OrderProductDetail> productMap) {
        OrderProductsResult.OrderProductDetail product = productMap.get(variantId);
        return CreateOrderSheetItemContext.builder()
                .productSnapshot(product.productSnapshot())
                .priceSnapshot(product.priceSnapshot())
                .quantity(quantity)
                .optionSnapshots(product.options())
                .build();
    }

    private CreateOrderSheetContext buildContext(OrdererProfileResult profile, List<CreateOrderSheetItemContext> items, LocalDateTime expiresAt) {
        return CreateOrderSheetContext.builder()
                .orderer(profile.orderer())
                .shippingAddress(profile.defaultShippingAddress())
                .items(items)
                .expiresAt(expiresAt)
                .build();
    }
}
