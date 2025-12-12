package com.example.order_service.api.order.application;

import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.domain.service.OrderDomainService;
import com.example.order_service.api.order.domain.service.OrderPriceCalculator;
import com.example.order_service.api.order.domain.service.dto.command.CouponSpec;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import com.example.order_service.api.order.domain.service.dto.result.PriceCalculateResult;
import com.example.order_service.api.order.infrastructure.OrderIntegrationService;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderIntegrationService orderIntegrationService;
    private final OrderPriceCalculator calculator;
    private final OrderDomainService orderDomainService;

    public CreateOrderResponse createOrder(CreateOrderDto dto){
        //주문 유저 조회
        OrderUserResponse user = orderIntegrationService.getOrderUser(dto.getUserPrincipal());
        //주문 상품 목록 조회
        List<OrderProductResponse> products = orderIntegrationService.getOrderProducts(dto.getOrderItemDtoList());
        //주문 상품 총가격 계산
        long subTotalPrice = calculator.calculateSubTotalPrice(dto.getOrderItemDtoList(), products);
        OrderCouponCalcResponse coupon = orderIntegrationService.getCoupon(dto.getUserPrincipal(), dto.getCouponId(), subTotalPrice);
        //할인 적용 최종 금액 계산
        PriceCalculateResult priceResult = calculator
                .calculateFinalPrice(dto.getPointToUse(), subTotalPrice, dto.getExpectedPrice(), user, coupon);

        OrderCreationContext creationContext =
                createCreationContext(dto, user, products, priceResult);
        OrderCreationResult orderCreationResult = orderDomainService.saveOrder(creationContext);
        return CreateOrderResponse.of(orderCreationResult);
    }

    private OrderCreationContext createCreationContext(CreateOrderDto dto,
                                                       OrderUserResponse user,
                                                       List<OrderProductResponse> products,
                                                       PriceCalculateResult priceResult){
        Map<Long, OrderProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, Function.identity()));

        List<OrderItemSpec> itemSpecs = dto.getOrderItemDtoList().stream()
                .map(item -> {
                    OrderProductResponse product = productMap.get(item.getProductVariantId());
                    return OrderItemSpec.of(product, item.getQuantity());
                }).toList();
        CouponSpec couponSpec = CouponSpec.from(priceResult.getCoupon());
        return OrderCreationContext.of(user.getUserId(), itemSpecs, couponSpec, priceResult.getUseToPoint(),
                dto.getDeliveryAddress(), priceResult.getFinalPaymentAmount());
    }

}
