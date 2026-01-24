package com.example.order_service.api.order.facade;

import com.example.order_service.api.order.domain.model.vo.*;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
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

@Component
public class OrderCreationContextMapper {

    public OrderCreationContext mapOrderCreationContext(OrderUserInfo user, CalculatedOrderAmounts amounts, OrderCouponInfo coupon,
                                                        CreateOrderCommand command, List<OrderProductInfo> products) {
        Orderer orderer = mapOrderer(user);
        OrderPriceDetail orderPriceDetail = mapOrderPriceDetail(amounts);
        CouponInfo couponInfo = mapCouponInfo(coupon);
        List<OrderItemCreationContext> orderItemCreationContexts = mapItemCreationContexts(command.getOrderItemCommands(), products);
        return OrderCreationContext.of(orderer, orderPriceDetail, couponInfo, orderItemCreationContexts, command.getDeliveryAddress());
    }

    private Orderer mapOrderer(OrderUserInfo user) {
        return Orderer.of(user.getUserId(), user.getUserName(), user.getPhoneNumber());
    }

    private OrderPriceDetail mapOrderPriceDetail(CalculatedOrderAmounts amounts) {
        return OrderPriceDetail.of(
                amounts.getTotalOriginalAmount(),
                amounts.getTotalProductDiscount(),
                amounts.getCouponDiscountAmount(),
                amounts.getUsePointAmount(),
                amounts.getFinalPaymentAmount());
    }

    private CouponInfo mapCouponInfo(OrderCouponInfo coupon) {
        return CouponInfo.of(coupon.getCouponId(), coupon.getCouponName(), coupon.getDiscountAmount());
    }

    private List<OrderItemCreationContext> mapItemCreationContexts(List<CreateOrderItemCommand> orderItemCommands, List<OrderProductInfo> products) {
        Map<Long, Integer> qtyMap = mapQtyByVariantId(orderItemCommands);
        return products.stream().map(product -> {
            Integer qty = qtyMap.get(product.getProductVariantId());
            OrderedProduct orderedProduct = OrderedProduct.of(product.getProductId(), product.getProductVariantId(), product.getSku(), product.getProductName(), product.getThumbnail());
            OrderItemPrice orderItemPrice = OrderItemPrice.of(product.getOriginalPrice(), product.getDiscountRate(), product.getDiscountAmount(), product.getDiscountedPrice());
            long lineTotal = product.getDiscountedPrice() * qty;
            List<OrderItemCreationContext.ItemOption> itemOptions = product.getProductOption().stream().map(o -> OrderItemCreationContext.ItemOption.of(o.getOptionTypeName(), o.getOptionValueName())).toList();
            return OrderItemCreationContext.of(orderedProduct, orderItemPrice, qty, lineTotal, itemOptions);
        }).toList();
    }

    private Map<Long, Integer> mapQtyByVariantId(List<CreateOrderItemCommand> orderItemCommands) {
        return orderItemCommands.stream()
                .collect(Collectors.toMap(CreateOrderItemCommand::getProductVariantId, CreateOrderItemCommand::getQuantity));
    }
}
