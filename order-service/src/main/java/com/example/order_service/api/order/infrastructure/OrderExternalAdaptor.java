package com.example.order_service.api.order.infrastructure;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.infrastructure.client.coupon.OrderCouponClientService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.payment.TossPaymentClientService;
import com.example.order_service.api.order.infrastructure.client.payment.dto.TossPaymentConfirmResponse;
import com.example.order_service.api.order.infrastructure.client.product.OrderProductClientService;
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
    private final OrderProductClientService orderProductClientService;
    private final OrderUserClientService orderUserClientService;
    private final OrderCouponClientService orderCouponClientService;
    private final TossPaymentClientService tossPaymentClientService;

    public OrderUserResponse getOrderUser(Long userId) {
        return orderUserClientService.getUserForOrder(userId);
    }

    public TossPaymentConfirmResponse confirmOrderPayment(Long orderId, String paymentKey, Long amount) {
        return tossPaymentClientService.confirmPayment(orderId, paymentKey, amount);
    }

    public OrderCouponDiscountResponse getCoupon(Long userId, Long couponId, Long subTotalPrice) {
        if (couponId == null) {
            return null;
        }
        return orderCouponClientService.calculateDiscount(userId, couponId, subTotalPrice);
    }

    public List<OrderProductResponse> getOrderProducts(List<CreateOrderItemDto> dtoList){
        List<Long> variantId = extractVariantIds(dtoList);
        List<OrderProductResponse> products = orderProductClientService.getProducts(variantId);
        verifyMissingProducts(variantId, products);
        verifyStockAvailability(dtoList, products);
        return products;
    }

    private List<Long> extractVariantIds(List<CreateOrderItemDto> dtoList){
        return dtoList.stream().map(CreateOrderItemDto::getProductVariantId)
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
        throw new NotFoundException("주문 상품중 존재하지 않은 상품이 있습니다. missingIds=" + missingIds);
    }

    private void verifyStockAvailability(List<CreateOrderItemDto> requestItems, List<OrderProductResponse> products) {
        Map<Long, Integer> stockMap = products.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, OrderProductResponse::getStockQuantity));

        for (CreateOrderItemDto item : requestItems) {
            Integer currentStock = stockMap.get(item.getProductVariantId());
            if(item.getQuantity() > currentStock) {
                throw new InsufficientException(String.format(
                        "재고가 부족합니다 (ProductVariantId: %d | 현재 재고: %d | 요청 수량: %d)",
                        item.getProductVariantId(), currentStock, item.getQuantity()
                ));
            }
        }
    }
}
