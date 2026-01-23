package com.example.order_service.api.order.infrastructure;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.infrastructure.client.coupon.OrderCouponClientService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.payment.TossPaymentClientService;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.OrderProductAdaptor;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.OrderUserClientService;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderExternalAdaptor {
    private final OrderProductAdaptor orderProductAdaptor;
    private final OrderUserClientService orderUserClientService;
    private final OrderCouponClientService orderCouponClientService;
    private final TossPaymentClientService tossPaymentClientService;

    public OrderUserResponse getOrderUser(Long userId) {
        return orderUserClientService.getUserForOrder(userId);
    }

    public OrderCouponDiscountResponse getCoupon(Long userId, Long couponId, Long subTotalPrice) {
        if (couponId == null) {
            return null;
        }
        return orderCouponClientService.calculateDiscount(userId, couponId, subTotalPrice);
    }

    public List<OrderProductResponse> getOrderProducts(List<CreateOrderItemCommand> dtoList){
        List<Long> variantId = extractVariantIds(dtoList);
        List<OrderProductResponse> products = orderProductAdaptor.getProducts(variantId);
        verifyMissingProducts(variantId, products);
        verifyStockAvailability(dtoList, products);
        return products;
    }

    public TossPaymentConfirmResponse confirmOrderPayment(String orderNo, String paymentKey, Long amount) {
        return tossPaymentClientService.confirmPayment(orderNo, paymentKey, amount);
    }

    public void cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        tossPaymentClientService.cancelPayment(paymentKey, cancelReason, cancelAmount);
    }

    private List<Long> extractVariantIds(List<CreateOrderItemCommand> dtoList){
        return dtoList.stream().map(CreateOrderItemCommand::getProductVariantId)
                .toList();
    }

    private void verifyMissingProducts(List<Long> requestIds, List<OrderProductResponse> products){
        if (products.size() == requestIds.size()) {
            return;
        }
        Set<Long> foundIds = products.stream().map(OrderProductResponse::getProductVariantId)
                .collect(Collectors.toSet());
        List<Long> missingIds = requestIds.stream().filter(id -> !foundIds.contains(id))
                .toList();
        throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND, "missing variantIds : " + missingIds);
    }

    private void verifyStockAvailability(List<CreateOrderItemCommand> requestItems, List<OrderProductResponse> products) {
        Map<Long, Integer> stockMap = products.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, OrderProductResponse::getStockQuantity));

        for (CreateOrderItemCommand item : requestItems) {
            Integer currentStock = stockMap.get(item.getProductVariantId());
            if(item.getQuantity() > currentStock) {
                throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_OUT_OF_STOCK,
                        String.format("(ProductVariantId: %d | 현재 재고: %d | 요청 수량: %d)",
                                item.getProductVariantId(), currentStock, item.getQuantity()));
            }
        }
    }
}
