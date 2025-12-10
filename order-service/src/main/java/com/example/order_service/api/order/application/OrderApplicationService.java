package com.example.order_service.api.order.application;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.infrastructure.client.coupon.OrderCouponClientService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.OrderProductClientService;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.OrderUserClientService;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderProductClientService orderProductClientService;
    private final OrderUserClientService orderUserClientService;
    private final OrderCouponClientService orderCouponClientService;

    public CreateOrderResponse createOrder(CreateOrderDto dto){
        OrderUserResponse user = getOrderUser(dto);
        List<OrderProductResponse> products = getOrderProducts(dto.getOrderItemDtoList());
        long totalPrice = calculateTotalPrice(dto.getOrderItemDtoList(), products);

        if(dto.getCouponId() != null){
            Long userId = dto.getUserPrincipal().getUserId();
            Long couponId = dto.getCouponId();
            OrderCouponCalcResponse calcResponse = orderCouponClientService.calculateDiscount(userId, couponId, totalPrice);
            totalPrice = calcResponse.getFinalPaymentAmount();
        }
        long finalPaymentPrice = totalPrice - dto.getPointToUse();

        if(finalPaymentPrice != dto.getExpectedPrice()){
            throw new OrderVerificationException("주문 금액이 변동되었습니다");
        }
        return null;
    }

    private long calculateTotalPrice(List<CreateOrderItemDto> requests, List<OrderProductResponse> responses){
        Map<Long, Integer> requestMap = requests.stream()
                .collect(Collectors.toMap(CreateOrderItemDto::getProductVariantId, CreateOrderItemDto::getQuantity));

        Map<Long, OrderProductResponse.UnitPrice> responseMap = responses.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, OrderProductResponse::getUnitPrice));

        return requestMap.entrySet().stream()
                .mapToLong(entry -> entry.getValue() * responseMap.get(entry.getKey()).getDiscountedPrice()).sum();
    }

    private OrderUserResponse getOrderUser(CreateOrderDto dto){
        OrderUserResponse user = orderUserClientService.getUserForOrder(dto.getUserPrincipal().getUserId());
        verifyEnoughPoints(dto.getPointToUse(), user);
        return user;
    }

    private void verifyEnoughPoints(Long useToPoint, OrderUserResponse user){
        if(useToPoint != null && useToPoint > 0) {
            if(useToPoint > user.getPointBalance()){
                throw new InsufficientException("포인트가 부족합니다");
            }
        }
    }

    // 주문 상품을 조회, 검증 후 응답 반환
    private List<OrderProductResponse> getOrderProducts(List<CreateOrderItemDto> dtoList){
        List<Long> reqVariantIds = extractVariantIds(dtoList);
        List<OrderProductResponse> products = orderProductClientService.getProducts(reqVariantIds);
        verifyMissingProducts(reqVariantIds, products);
        return products;
    }

    private List<Long> extractVariantIds(List<CreateOrderItemDto> dtoList){
        return dtoList.stream().map(CreateOrderItemDto::getProductVariantId)
                .toList();
    }

    // 요청 id 와 반환값이 일치하는지 검증
    private void verifyMissingProducts(List<Long> productVariantIds, List<OrderProductResponse> products){
        if(products.size() != productVariantIds.size()){
            Set<Long> resultIds = products.stream().map(OrderProductResponse::getProductVariantId)
                    .collect(Collectors.toSet());

            List<Long> missingIds = productVariantIds.stream().filter(id -> !resultIds.contains(id))
                    .toList();

            throw new NotFoundException("주문 상품중 존재하지 않은 상품이 있습니다. missingIds=" + missingIds);
        }
    }
}
