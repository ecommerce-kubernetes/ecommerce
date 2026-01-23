package com.example.order_service.api.order.facade;

import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderDtoMapper {
    public com.example.order_service.api.order.domain.service.dto.command.CreateOrderCommand assembleOrderCommand(CreateOrderCommand dto,
                                                                                                                  OrderUserResponse user,
                                                                                                                  List<OrderProductResponse> products,
                                                                                                                  PriceCalculateResult priceResult) {

        List<com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand> itemCommands = mapToItemCommands(dto.getOrderItemDtoList(), products);

        return com.example.order_service.api.order.domain.service.dto.command.CreateOrderCommand.builder()
                .userId(user.getUserId())
                .itemCommands(itemCommands)
                .orderPriceInfo(priceResult.getOrderPriceInfo())
                .deliveryAddress(dto.getDeliveryAddress())
                .appliedCoupon(priceResult.getAppliedCoupon())
                .build();
    }

    public List<com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand> mapToItemCommands(List<CreateOrderItemCommand> items,
                                                                                                                         List<OrderProductResponse> products) {
        Map<Long, OrderProductResponse> productMap = products
                .stream().collect(Collectors.toMap(OrderProductResponse::getProductVariantId, Function.identity()));

        return items.stream()
                .map(item -> {
                    OrderProductResponse product = productMap.get(item.getProductVariantId());
                    com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand.UnitPrice commandPrice = com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand.UnitPrice.builder()
                            .originalPrice(product.getUnitPrice().getOriginalPrice())
                            .discountRate(product.getUnitPrice().getDiscountRate())
                            .discountAmount(product.getUnitPrice().getDiscountAmount())
                            .discountedPrice(product.getUnitPrice().getDiscountedPrice())
                            .build();

                    List<com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand.ItemOption> commandOptions = product.getItemOptions().stream()
                            .map(option -> com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand.ItemOption.builder()
                                    .optionTypeName(option.getOptionTypeName())
                                    .optionValueName(option.getOptionValueName())
                                    .build())
                            .toList();

                    return com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand.builder()
                            .productId(product.getProductId())
                            .productVariantId(product.getProductVariantId())
                            .sku(product.getSku())
                            .productName(product.getProductName())
                            .thumbnailUrl(product.getThumbnailUrl())
                            .unitPrice(commandPrice)
                            .quantity(item.getQuantity())
                            .lineTotal(product.getUnitPrice().getDiscountedPrice() * item.getQuantity())
                            .itemOptions(commandOptions)
                            .build();
                }).toList();
    }
}
