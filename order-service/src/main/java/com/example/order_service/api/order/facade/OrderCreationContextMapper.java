package com.example.order_service.api.order.facade;

import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.CouponSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrderPriceSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext.OrdererSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.CalculatedOrderAmounts;
import com.example.order_service.api.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderUserInfo;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext.*;

@Component
public class OrderCreationContextMapper {

    public OrderCreationContext mapOrderCreationContext(OrderUserInfo user, CalculatedOrderAmounts amounts, OrderCouponInfo coupon,
                                                        CreateOrderCommand command, List<OrderProductInfo> products) {
        OrdererSpec orderer = mapToOrdererSpec(user);
        OrderPriceSpec orderPrice = mapToOrderPriceSpec(amounts);
        CouponSpec couponInfo = mapToCouponInfo(coupon);
        List<OrderItemCreationContext> orderItemCreationContexts = mapToItemCreationContexts(command.getOrderItemCommands(), products);
        return OrderCreationContext.of(orderer, orderPrice, couponInfo, orderItemCreationContexts, command.getDeliveryAddress());
    }

    private OrdererSpec mapToOrdererSpec(OrderUserInfo user) {
        return OrdererSpec.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    private OrderPriceSpec mapToOrderPriceSpec(CalculatedOrderAmounts amounts) {
        return OrderPriceSpec.of(
                amounts.getTotalOriginalAmount(),
                amounts.getTotalProductDiscount(),
                amounts.getCouponDiscountAmount(),
                amounts.getUsePointAmount(),
                amounts.getFinalPaymentAmount());
    }

    private CouponSpec mapToCouponInfo(OrderCouponInfo coupon) {
        if (coupon == null || coupon.getCouponId() == null) {
            return null;
        }
        return CouponSpec.of(coupon.getCouponId(), coupon.getCouponName(), coupon.getDiscountAmount());
    }

    private List<OrderItemCreationContext> mapToItemCreationContexts(List<CreateOrderItemCommand> orderItemCommands,
                                                                     List<OrderProductInfo> products) {
        Map<Long, Integer> qtyMap = mapQtyByVariantId(orderItemCommands);
        return products.stream().map(product -> {
            Integer qty = qtyMap.get(product.getProductVariantId());
            ProductSpec productSpec = ProductSpec.of(product.getProductId(), product.getProductVariantId(),
                    product.getSku(), product.getProductName(), product.getThumbnail());
            PriceSpec priceSpec = PriceSpec.of(product.getOriginalPrice(), product.getDiscountRate(),
                    product.getDiscountAmount(), product.getDiscountedPrice());
            long lineTotal = product.getDiscountedPrice() * qty;
            List<CreateItemOptionSpec> createItemOptionSpecs = product.getProductOption().stream().map(o ->
                    CreateItemOptionSpec.of(o.getOptionTypeName(), o.getOptionValueName())).toList();
            return OrderItemCreationContext.of(productSpec, priceSpec, qty, lineTotal, createItemOptionSpecs);
        }).toList();
    }

    private Map<Long, Integer> mapQtyByVariantId(List<CreateOrderItemCommand> orderItemCommands) {
        return orderItemCommands.stream()
                .collect(Collectors.toMap(CreateOrderItemCommand::getProductVariantId, CreateOrderItemCommand::getQuantity));
    }
}
