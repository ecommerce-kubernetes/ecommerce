package com.example.order_service.api.order.domain.service;

import com.example.order_service.dto.OrderCalculationResult;
import com.example.order_service.dto.OrderValidationData;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.order.application.dto.result.CreateOrderResponse;
import com.example.order_service.api.order.controller.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.api.order.domain.model.Orders;
import com.example.order_service.api.common.exception.BadRequestException;
import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.domain.repository.OrdersRepository;
import com.example.order_service.service.client.CouponClientService;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.service.client.UserClientService;
import com.example.order_service.service.client.dto.CouponResponse;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.service.client.dto.UserBalanceResponse;
import com.example.order_service.api.order.application.dto.command.CreateOrderDto;
import com.example.order_service.service.event.OrderEndMessageEvent;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    private final OrdersRepository ordersRepository;
    private final CartProductClientService cartProductClientService;
    private final UserClientService userClientService;
    private final CouponClientService couponClientService;

    @Transactional
    public CreateOrderResponse saveOrder(Long userId, CreateOrderRequest request) {
        OrderValidationData orderValidationData = fetchRequiredData(userId, request);
        OrderCalculationResult calcResult = calculateOrderTotals(request, orderValidationData);
        validateOrder(calcResult, request, orderValidationData);

        Orders order = Orders.create(userId, request, calcResult);
        Orders save = ordersRepository.save(order);
        String url = buildSubscribeUrl(save.getId());
        eventPublisher.publishEvent(new PendingOrderCreatedEvent(this, save));
        return new CreateOrderResponse(save, url);
    }

    public CreateOrderResponse saveOrder(CreateOrderDto createOrderDto){
        return null;
    }

    public PageDto<OrderResponse> getOrderList(Pageable pageable, Long userId, String year, String keyword) {
        return null;
    }

    @Transactional
    public void completeOrder(Long orderId){
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new NotFoundException("주문을 찾을 수 없음"));
        order.complete();
        eventPublisher.publishEvent(new OrderEndMessageEvent(this, order.getId(), order.getStatus()));
    }

    @Transactional
    public void cancelOrder(Long orderId){
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new NotFoundException("주문을 찾을 수 없음"));
        order.cancel();
        eventPublisher.publishEvent(new OrderEndMessageEvent(this, order.getId(), order.getStatus()));
    }

    private String buildSubscribeUrl(Long orderId){
        return "http://test.com/" + orderId + "/subscribe";
    }

    private OrderCalculationResult calculateOrderTotals(CreateOrderRequest request, OrderValidationData data){
        Map<Long, Integer> quantityMap = request.toQuantityMap();
        Map<Long, CartProductResponse> productByVariantId = data.toProductByVariantId();
        long originOrderItemsPrice = 0;
        long productDiscountAmount = 0;
        long discountedOrderItemPrice = 0;

        long couponDiscount = (data.getCoupon() != null) ? calcCouponDiscount(data.getCoupon(), discountedOrderItemPrice) : 0;
        long amountToPay = discountedOrderItemPrice - request.getPointToUse() - couponDiscount;

        return new OrderCalculationResult(quantityMap, productByVariantId, originOrderItemsPrice,
                productDiscountAmount, discountedOrderItemPrice, couponDiscount, amountToPay);
    }

    private void validateOrder(OrderCalculationResult result, CreateOrderRequest createOrderRequest, OrderValidationData data){
        if(data.getUserBalance().getPointAmount() < createOrderRequest.getPointToUse()){
            throw new InsufficientException("사용가능한 포인트가 부족");
        }

        if(data.getCoupon() != null && result.getDiscountedOrderItemsPrice() < data.getCoupon().getMinPurchaseAmount()) {
            throw new InsufficientException("결제 금액이 쿠폰 최소 결제 금액 미만");
        }

        if(result.getAmountToPay() != createOrderRequest.getExpectedPrice()){
            throw new BadRequestException("예상 결제 금액과 실제 결제 금액이 맞지 않습니다");
        }

        if(result.getAmountToPay() > data.getUserBalance().getCashAmount()){
            throw new InsufficientException("잔액이 부족합니다");
        }
    }

    private OrderValidationData fetchRequiredData(Long userId, CreateOrderRequest createOrderRequest){
        List<CartProductResponse> product = cartProductClientService.getProducts(createOrderRequest.getItemsVariantId());
        UserBalanceResponse userBalance = userClientService.fetchBalance();
        CouponResponse couponInfo = (createOrderRequest.getCouponId() != null) ? couponClientService.fetchCouponByUserCouponId(userId, createOrderRequest.getCouponId())
                : null;

        return new OrderValidationData(product, userBalance, couponInfo);
    }

    private long calcCouponDiscount(CouponResponse coupon, long totalItemPrice){
        if(coupon.getDiscountType().equals("AMOUNT")){
            return coupon.getDiscountValue();
        }
        long couponDiscount = (long) (totalItemPrice * (coupon.getDiscountValue() / 100.0));
        if(couponDiscount > coupon.getMaxDiscountAmount()){
            couponDiscount = coupon.getMaxDiscountAmount();
        }
        return couponDiscount;
    }
}
