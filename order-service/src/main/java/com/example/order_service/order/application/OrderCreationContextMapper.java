package com.example.order_service.order.application;

import com.example.order_service.order.application.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.dto.command.CreateOrderItemCommand;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import com.example.order_service.order.application.dto.result.OrderPaymentResult;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import com.example.order_service.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.order.domain.service.dto.command.OrderCreationContext.CouponSpec;
import com.example.order_service.order.domain.service.dto.command.OrderCreationContext.OrderPriceSpec;
import com.example.order_service.order.domain.service.dto.command.OrderCreationContext.OrdererSpec;
import com.example.order_service.order.domain.service.dto.command.OrderItemCreationContext;
import com.example.order_service.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.order.domain.service.dto.result.CalculatedOrderAmounts;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.order_service.order.domain.service.dto.command.OrderItemCreationContext.*;

@Component
public class OrderCreationContextMapper {

    public PaymentCreationContext mapPaymentCreationContext(OrderPaymentResult.Payment orderPaymentInfo) {
        return PaymentCreationContext.builder()
                .orderNo(orderPaymentInfo.orderNo())
                .paymentKey(orderPaymentInfo.paymentKey())
                .amount(orderPaymentInfo.totalAmount())
                .status(orderPaymentInfo.status())
                .method(orderPaymentInfo.method())
                .approvedAt(orderPaymentInfo.approvedAt())
                .build();
    }

    public OrderCreationContext mapOrderCreationContext(OrderUserResult.OrdererInfo user, CalculatedOrderAmounts amounts, OrderCouponResult.CouponValidation coupon,
                                                        CreateOrderCommand command, List<OrderProductResult.Info> products) {
        OrdererSpec orderer = mapToOrdererSpec(user);
        OrderPriceSpec orderPrice = mapToOrderPriceSpec(amounts);
        CouponSpec couponInfo = mapToCouponInfo(coupon);
        List<OrderItemCreationContext> orderItemCreationContexts = mapToItemCreationContexts(command.getOrderItemCommands(), products);
        return OrderCreationContext.of(orderer, orderPrice, couponInfo, orderItemCreationContexts, command.getDeliveryAddress());
    }

    private OrdererSpec mapToOrdererSpec(OrderUserResult.OrdererInfo user) {
        return OrdererSpec.builder()
                .userId(user.userId())
                .userName(user.ordererName())
                .phoneNumber(user.ordererPhone())
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

    private CouponSpec mapToCouponInfo(OrderCouponResult.CouponValidation coupon) {
        if (coupon == null || coupon.couponBenefit().couponId() == null) {
            return null;
        }
        return CouponSpec.of(coupon.couponBenefit().couponId(), coupon.couponBenefit().couponName(), coupon.couponBenefit().discountAmount());
    }

    private List<OrderItemCreationContext> mapToItemCreationContexts(List<CreateOrderItemCommand> orderItemCommands,
                                                                     List<OrderProductResult.Info> products) {
        Map<Long, Integer> qtyMap = mapQtyByVariantId(orderItemCommands);
        return products.stream().map(product -> {
            Integer qty = qtyMap.get(product.productVariantId());
            ProductSpec productSpec = ProductSpec.of(product.productId(), product.productVariantId(),
                    product.sku(), product.productName(), product.thumbnail());
            PriceSpec priceSpec = PriceSpec.of(product.originalPrice().longValue(), product.discountRate(),
                    product.discountAmount().longValue(), product.discountedPrice().longValue());
            long lineTotal = product.discountedPrice().longValue() * qty;
            List<CreateItemOptionSpec> createItemOptionSpecs = product.options().stream().map(o ->
                    CreateItemOptionSpec.of(o.optionTypeName(), o.optionValueName())).toList();
            return OrderItemCreationContext.of(productSpec, priceSpec, qty, lineTotal, createItemOptionSpecs);
        }).toList();
    }

    private Map<Long, Integer> mapQtyByVariantId(List<CreateOrderItemCommand> orderItemCommands) {
        return orderItemCommands.stream()
                .collect(Collectors.toMap(CreateOrderItemCommand::getProductVariantId, CreateOrderItemCommand::getQuantity));
    }
}
