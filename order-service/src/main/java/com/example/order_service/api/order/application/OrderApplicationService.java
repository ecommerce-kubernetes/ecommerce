package com.example.order_service.api.order.application;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.context.PriceCalculateResult;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderProductClientService orderProductClientService;
    private final OrderUserClientService orderUserClientService;
    private final OrderCouponClientService orderCouponClientService;
    private final OrderDomainService orderDomainService;

    public CreateOrderResponse createOrder(CreateOrderDto dto){
        //주문 유저 조회
        OrderUserResponse user = getOrderUser(dto);
        //주문 상품 목록 조회
        List<OrderProductResponse> products = getOrderProducts(dto.getOrderItemDtoList());
        //주문 가격 정보 계산
        PriceCalculateResult priceResult = calculateOrderPrice(dto, products, user);

        OrderCreationContext creationContext = OrderCreationContext.of(user, dto.getOrderItemDtoList(), products,
                priceResult, dto.getDeliveryAddress());
        OrderCreationResult orderCreationResult = orderDomainService.saveOrder(creationContext);
        return null;
    }

    private OrderCouponCalcResponse getOrderCouponCalcResponse(CreateOrderDto dto, long totalPrice) {
        return Optional.ofNullable(dto.getCouponId())
                .map(couponId -> orderCouponClientService.calculateDiscount(
                        dto.getUserPrincipal().getUserId(),
                        dto.getCouponId(),
                        totalPrice
                ))
                .orElse(null);
    }

    private OrderUserResponse getOrderUser(CreateOrderDto dto){
        return orderUserClientService.getUserForOrder(dto.getUserPrincipal().getUserId());
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

    private void verifyEnoughPoints(Long useToPoint, OrderUserResponse user){
        if(useToPoint != null && useToPoint > 0) {
            if(useToPoint > user.getPointBalance()){
                throw new InsufficientException("포인트가 부족합니다");
            }
        }
    }

    private long calculateRowTotalPrice(List<CreateOrderItemDto> requests, List<OrderProductResponse> responses){
        Map<Long, Integer> quantityByVariantId = requests.stream()
                .collect(Collectors.toMap(CreateOrderItemDto::getProductVariantId, CreateOrderItemDto::getQuantity));
                                                                                    
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = responses.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, OrderProductResponse::getUnitPrice));

        return quantityByVariantId.entrySet().stream()
                .mapToLong(
                        entry -> entry.getValue() * unitPriceByVariantId.get(entry.getKey()).getDiscountedPrice())
                .sum();
    }

    private PriceCalculateResult calculateOrderPrice(CreateOrderDto dto, List<OrderProductResponse> products, OrderUserResponse user){
        verifyEnoughPoints(dto.getPointToUse(), user);

        long originTotalPrice = calculateRowTotalPrice(dto.getOrderItemDtoList(), products);
        OrderCouponCalcResponse coupon = getOrderCouponCalcResponse(dto, originTotalPrice);
        long priceAfterCoupon =  (coupon != null) ? 300L : originTotalPrice;
        long finalPaymentPrice = priceAfterCoupon - dto.getPointToUse();

        if(finalPaymentPrice != dto.getExpectedPrice()) {
            throw new OrderVerificationException("주문 금액이 변동되었습니다");
        }

        return PriceCalculateResult.of(originTotalPrice, finalPaymentPrice, coupon, dto.getPointToUse());
    }
}
