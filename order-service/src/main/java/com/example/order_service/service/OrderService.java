package com.example.order_service.service;

import com.example.order_service.dto.OrderCalculationResult;
import com.example.order_service.dto.OrderValidationData;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.BadRequestException;
import com.example.order_service.exception.InsufficientException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.client.CouponClientService;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.UserClientService;
import com.example.order_service.service.client.dto.CouponResponse;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.service.client.dto.UserBalanceResponse;
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
    private final ProductClientService productClientService;
    private final UserClientService userClientService;
    private final CouponClientService couponClientService;

    @Transactional
    public CreateOrderResponse saveOrder(Long userId, OrderRequest request) {
        OrderValidationData orderValidationData = fetchRequiredData(userId, request);
        OrderCalculationResult calcResult = calculateOrderTotals(request, orderValidationData);
        validateOrder(calcResult, request, orderValidationData);

        Orders order = Orders.create(userId, request, calcResult);
        Orders save = ordersRepository.save(order);
        String url = buildSubscribeUrl(save.getId());
        eventPublisher.publishEvent(new PendingOrderCreatedEvent(this, save));
        return new CreateOrderResponse(save, url);
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

    private OrderCalculationResult calculateOrderTotals(OrderRequest request, OrderValidationData data){
        Map<Long, Integer> quantityMap = request.toQuantityMap();
        Map<Long, ProductResponse> productByVariantId = data.toProductByVariantId();
        long originOrderItemsPrice = 0;
        long productDiscountAmount = 0;
        long discountedOrderItemPrice = 0;

        for (Map.Entry<Long, ProductResponse> e : productByVariantId.entrySet()) {
            Long variantId = e.getKey();
            ProductResponse product = e.getValue();
            Integer quantity = quantityMap.getOrDefault(variantId, 0);
            originOrderItemsPrice += product.getProductPrice().getUnitPrice() * quantity;
            productDiscountAmount += product.getProductPrice().getDiscountAmount() * quantity;
            discountedOrderItemPrice += product.getProductPrice().getDiscountedPrice() * quantity;
        }

        long couponDiscount = (data.getCoupon() != null) ? calcCouponDiscount(data.getCoupon(), discountedOrderItemPrice) : 0;
        long amountToPay = discountedOrderItemPrice - request.getPointToUse() - couponDiscount;

        return new OrderCalculationResult(quantityMap, productByVariantId, originOrderItemsPrice,
                productDiscountAmount, discountedOrderItemPrice, couponDiscount, amountToPay);
    }

    private void validateOrder(OrderCalculationResult result, OrderRequest orderRequest, OrderValidationData data){
        if(data.getUserBalance().getPointAmount() < orderRequest.getPointToUse()){
            throw new InsufficientException("사용가능한 포인트가 부족");
        }

        if(data.getCoupon() != null && result.getDiscountedOrderItemsPrice() < data.getCoupon().getMinPurchaseAmount()) {
            log.info("discountedOrderItemPrice = {}" , result.getDiscountedOrderItemsPrice());
            throw new InsufficientException("결제 금액이 쿠폰 최소 결제 금액 미만");
        }

        if(result.getAmountToPay() != orderRequest.getExpectedPrice()){
            throw new BadRequestException("예상 결제 금액과 실제 결제 금액이 맞지 않습니다");
        }

        if(result.getAmountToPay() > data.getUserBalance().getCashAmount()){
            throw new InsufficientException("잔액이 부족합니다");
        }
    }

    private OrderValidationData fetchRequiredData(Long userId, OrderRequest orderRequest){
        List<ProductResponse> product = productClientService.fetchProductByVariantIds(orderRequest.getItemsVariantId());
        log.info("product {}", product.get(0).getProductId());
        UserBalanceResponse userBalance = userClientService.fetchBalanceByUserId(userId);
        log.info("user {}", userBalance.getUserId());
        CouponResponse couponInfo = (orderRequest.getCouponId() != null) ? couponClientService.fetchCouponByUserCouponId(userId, orderRequest.getCouponId())
                : null;
        log.info("coupon {}", couponInfo.getDiscountValue());

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
