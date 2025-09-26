package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.common.MessagePath;
import com.example.order_service.dto.OrderCalculationResult;
import com.example.order_service.dto.OrderValidationData;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.BadRequestException;
import com.example.order_service.exception.InsufficientException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.exception.OrderVerificationException;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.client.CouponClientService;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.UserClientService;
import com.example.order_service.service.client.dto.CouponResponse;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.service.client.dto.UserBalanceResponse;
import com.example.order_service.service.event.OrderEndMessageEvent;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper mapper;
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
        UserBalanceResponse userBalance = userClientService.fetchBalanceByUserId(userId);
        CouponResponse couponInfo = (orderRequest.getCouponId() != null) ? couponClientService.fetchCouponByUserCouponId(orderRequest.getCouponId())
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

    public PageDto<OrderResponse> getOrderList(Pageable pageable, Long userId, String year, String keyword) {
        return null;
    }

    @Transactional
    public void finalizeOrder(Map<Object, Object> sagaState){
        ProductStockDeductedEvent prodEvent = mapper.convertValue(sagaState.get("product"), ProductStockDeductedEvent.class);
        UserCashDeductedEvent userEvent = mapper.convertValue(sagaState.get("user"), UserCashDeductedEvent.class);
        CouponUsedSuccessEvent couponEvent = mapper.convertValue(sagaState.get("coupon"), CouponUsedSuccessEvent.class);
        boolean isVerified = verifyPayment(prodEvent, userEvent, couponEvent);
        String orderStatus = sagaState.get("orderId").toString();
        Long orderId = Long.parseLong(orderStatus);
        Orders finalizedOrder;
        if(isVerified){
            finalizedOrder = updateCompleteOrder(orderId, prodEvent, couponEvent, userEvent);
        } else {
            finalizedOrder = updateFailOrder(orderId);
        }
        eventPublisher.publishEvent(new OrderEndMessageEvent(this, finalizedOrder.getId(), finalizedOrder.getStatus()));

        if(!isVerified){
            throw new OrderVerificationException("검증 실패");
        }
    }

    @Transactional
    public void failOrder(Long orderId){
        updateFailOrder(orderId);
        //TODO SSE 응답을 위해 메시지 발행

    }

    private void updateOrderItems(List<OrderItems> orderItems, List<DeductedProduct> products){
        Map<Long, DeductedProduct> productMap = products
                .stream().collect(Collectors.toMap(DeductedProduct::getProductVariantId, p -> p));
        for (OrderItems orderItem : orderItems){
            DeductedProduct product = productMap.get(orderItem.getProductVariantId());
            String options = convertToJson(product.getOptions());
            orderItem.setProductData(product.getProductId(),
                    product.getProductName(), options, product.getPriceInfo().getPrice(),
                    product.getPriceInfo().getDiscountRate(), product.getPriceInfo().getFinalPrice(),
                    product.getPriceInfo().getFinalPrice() * orderItem.getQuantity());
        }
    }
    private boolean verifyPayment(ProductStockDeductedEvent productEvent,
                                  UserCashDeductedEvent userEvent,
                                  CouponUsedSuccessEvent couponEvent){

        long finalItemsPrice = calcOrderItemFinalPrice(productEvent);
        log.info("finalItemPrice =  {}", finalItemsPrice);
        //최소 결제 가격 만족 여부
        if(couponEvent.getMinPurchaseAmount() > finalItemsPrice){
            log.info("최소 결제 금액 부족");
            return false;
        }
        long couponDiscount = calcCouponDiscount(couponEvent, finalItemsPrice);
        // 최대 할인금액보다 할인 금액이 높으면 최대 할인 금액을 적용
        if(couponDiscount > couponEvent.getMaxDiscountAmount()){
            couponDiscount = couponEvent.getMaxDiscountAmount();
        }
        // 검증 수행
        return userEvent.getExpectTotalAmount() == finalItemsPrice - couponDiscount;
    }

    //주문한 상품들의 최종 가격 : (할인적용 가격 * 수량) 합계

    private long calcOrderItemFinalPrice(ProductStockDeductedEvent event){
        return event.getDeductedProducts().stream()
                .mapToLong(item -> item.getPriceInfo().getFinalPrice() * item.getQuantity())
                .sum();
    }

    private long calcCouponDiscount(CouponUsedSuccessEvent event, long finalItemsPrice){
        if(event.getDiscountType() == DiscountType.AMOUNT){
            return event.getDiscountValue();
        }

        return (long) (finalItemsPrice * (event.getDiscountValue() / 100.0));
    }

    //주문 원 가격
    private long calcOrderOriginPrice(ProductStockDeductedEvent event){
        return event.getDeductedProducts().stream()
                .mapToLong(item -> (long) item.getPriceInfo().getPrice() * item.getQuantity())
                .sum();
    }

    //주문 상품 할인 금액
    private long calcOrderItemDiscount(ProductStockDeductedEvent event){
        return event.getDeductedProducts().stream()
                .mapToLong(item -> item.getPriceInfo().getDiscountAmount() * item.getQuantity())
                .sum();
    }

    private String buildSubscribeUrl(Long orderId){
        return "http://test.com/" + orderId + "/subscribe";
    }

    private String convertToJson(List<ItemOption> itemOptions){
        try{
            return mapper.writeValueAsString(itemOptions);
        } catch (JsonProcessingException e){
            throw new RuntimeException("Failed Convert To JSON");
        }
    }
    private String convertToJsonResponse(List<ItemOptionResponse> itemOptions){
        try{
            return mapper.writeValueAsString(itemOptions);
        } catch (JsonProcessingException e){
            throw new RuntimeException("Failed Convert To JSON");
        }
    }

    private Orders updateFailOrder(Long orderId){
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(MessagePath.ORDER_NOT_FOUND));
        order.cancel();
        return order;
    }
    private Orders updateCompleteOrder(Long orderId, ProductStockDeductedEvent prodEvent, CouponUsedSuccessEvent couponEvent,
                                     UserCashDeductedEvent userEvent){
        Orders order = ordersRepository.findWithOrderItemsById(orderId)
                .orElseThrow(() -> new NotFoundException(MessagePath.ORDER_NOT_FOUND));
        long originPrice = calcOrderOriginPrice(prodEvent);
        long prodDiscount = calcOrderItemDiscount(prodEvent);
        long couponDiscount = calcCouponDiscount(couponEvent, calcOrderItemFinalPrice(prodEvent));
        long reservedDiscount = userEvent.getReservedPointAmount();
        long payment = userEvent.getReservedCashAmount();
        order.setPriceInfo(originPrice, prodDiscount, couponDiscount, reservedDiscount, payment);
        updateOrderItems(order.getOrderItems(), prodEvent.getDeductedProducts());
        order.complete();
        return order;
    }
}
