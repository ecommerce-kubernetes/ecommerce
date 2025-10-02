package com.example.order_service.service.event;

import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class PendingOrderCreatedEvent extends ApplicationEvent {
    private Long orderId;
    private Long userId;
    private Long couponId;
    private Long usedPoint;
    private Long amountToPay;
    private String status;
    private LocalDateTime createdAt;
    private Map<Long, Integer> variantIdQuantiyMap;

    // 생성자를 통해 이벤트를 발행한 객체와 데이터를 추가
    public PendingOrderCreatedEvent(Object source, Orders order){
        super(source);
        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.couponId = order.getUsedCouponId();
        this.usedPoint = order.getPointDiscount();
        this.status = order.getStatus();
        this.createdAt = order.getCreateAt();
        this.variantIdQuantiyMap = order.getOrderItems()
                .stream().collect(Collectors.toMap(OrderItems::getProductVariantId, OrderItems::getQuantity));
        this.amountToPay = order.getAmountToPay();
    }

    public PendingOrderCreatedEvent(Object source, Long orderId, Long userId, Long couponId,
                                    Long usedPoint, Long amountToPay, String status, LocalDateTime createdAt,
                                    Map<Long, Integer> variantIdQuantiyMap) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.couponId = couponId;
        this.usedPoint = usedPoint;
        this.amountToPay = amountToPay;
        this.status = status;
        this.createdAt = createdAt;
        this.variantIdQuantiyMap = variantIdQuantiyMap;
    }
}
