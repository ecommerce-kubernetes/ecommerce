package com.example.order_service.service;

import com.example.common.*;
import com.example.order_service.common.MessagePath;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.exception.OrderVerificationException;
import com.example.order_service.repository.OrdersRepository;
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

    @Transactional
    public CreateOrderResponse saveOrder(Long userId, OrderRequest request) {
        Orders order = new Orders(userId, "PENDING", request.getDeliveryAddress());
        List<OrderItems> orderItems = request.getItems().stream().map(item -> new OrderItems(item.getProductVariantId(), item.getQuantity()))
                .toList();
        order.addOrderItems(orderItems);
        Orders save = ordersRepository.save(order);
        String url = buildSubscribeUrl(save.getId());
        eventPublisher.publishEvent(new PendingOrderCreatedEvent(this, save, request));
        return new CreateOrderResponse(save, url);
    }

    public PageDto<OrderResponse> getOrderList(Pageable pageable, Long userId, String year, String keyword) {
        return null;
    }

    @Transactional
    public void finalizeOrder(Map<Object, Object> sagaState){
        ProductStockDeductedEvent prodEvent = mapper.convertValue(sagaState.get("product"), ProductStockDeductedEvent.class);
        UserCacheDeductedEvent userEvent = mapper.convertValue(sagaState.get("user"), UserCacheDeductedEvent.class);
        CouponUsedSuccessEvent couponEvent = mapper.convertValue(sagaState.get("coupon"), CouponUsedSuccessEvent.class);
        boolean isVerified = verifyPayment(prodEvent, userEvent, couponEvent);
        Long orderId = (Long) sagaState.get("orderId");
        if(isVerified){
            updateCompleteOrder(orderId, prodEvent, couponEvent, userEvent);
            //TODO SSE 응답을 위해 메시지 발행
        } else {
            updateFailOrder(orderId);
            throw new OrderVerificationException("검증 실패");
        }
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
                                  UserCacheDeductedEvent userEvent,
                                  CouponUsedSuccessEvent couponEvent){

        long finalItemsPrice = calcOrderItemFinalPrice(productEvent);
        //최소 결제 가격 만족 여부
        if(couponEvent.getMinPurchaseAmount() > finalItemsPrice){
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

    private void updateFailOrder(Long orderId){
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(MessagePath.ORDER_NOT_FOUND));
        order.cancel();
    }
    private void updateCompleteOrder(Long orderId, ProductStockDeductedEvent prodEvent, CouponUsedSuccessEvent couponEvent,
                                     UserCacheDeductedEvent userEvent){
        Orders order = ordersRepository.findWithOrderItemsById(orderId)
                .orElseThrow(() -> new NotFoundException(MessagePath.ORDER_NOT_FOUND));
        long originPrice = calcOrderOriginPrice(prodEvent);
        long prodDiscount = calcOrderItemDiscount(prodEvent);
        long couponDiscount = calcCouponDiscount(couponEvent, calcOrderItemFinalPrice(prodEvent));
        long reservedDiscount = userEvent.getReservedPointAmount();
        long payment = userEvent.getReservedCacheAmount();
        order.setPriceInfo(originPrice, prodDiscount, couponDiscount, reservedDiscount, payment);
        updateOrderItems(order.getOrderItems(), prodEvent.getDeductedProducts());
        order.complete();
    }

}
