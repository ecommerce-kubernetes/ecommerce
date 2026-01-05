package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class OrderCreationContext {
    private Long userId;
    private List<OrderItemSpec> itemSpecs;
    private PriceCalculateResult priceResult;
    private String deliveryAddress;

    @Builder
    private OrderCreationContext(Long userId, List<OrderItemSpec> itemSpecs, PriceCalculateResult priceResult,
                                 String deliveryAddress){
        this.userId = userId;
        this.itemSpecs = itemSpecs;
        this.priceResult = priceResult;
        this.deliveryAddress = deliveryAddress;
    }

    public static OrderCreationContext of(Long userId, List<OrderItemSpec> itemSpecs, PriceCalculateResult priceResult, String deliveryAddress){
        return OrderCreationContext.builder()
                .userId(userId)
                .itemSpecs(itemSpecs)
                .priceResult(priceResult)
                .deliveryAddress(deliveryAddress)
                .build();
    }

    public static OrderCreationContext from(CreateOrderDto dto, OrderUserResponse user, List<OrderProductResponse> products, PriceCalculateResult priceResult) {
        Map<Long, OrderProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, Function.identity()));
        List<OrderItemSpec> itemSpecs = dto.getOrderItemDtoList().stream()
                .map(item -> {
                    OrderProductResponse product = productMap.get(item.getProductVariantId());
                    return OrderItemSpec.of(product, item.getQuantity());
                }).toList();
        return OrderCreationContext.of(user.getUserId(), itemSpecs, priceResult, dto.getDeliveryAddress());
    }
}
