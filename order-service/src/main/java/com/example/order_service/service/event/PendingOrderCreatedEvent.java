package com.example.order_service.service.event;

import com.example.common.OrderProduct;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.entity.Orders;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.List;

// 주문 저장 이벤트 ApplicationEvent 를 상속
@Getter
public class PendingOrderCreatedEvent extends ApplicationEvent {
    private Long orderId;
    private Long userId;
    private String status;
    private LocalDateTime createdAt;
    // 저장된 주문 데이터
    private List<OrderProduct> orderProducts;
    private OrderRequest orderRequest;


    // 생성자를 통해 이벤트를 발행한 객체와 데이터를 추가
    public PendingOrderCreatedEvent(Object source, Orders order, OrderRequest orderRequest){
        super(source);
        this.orderId = order.getId();
        this.userId = order.getUserId();
        this.status = order.getStatus();
        this.createdAt = order.getCreateAt();
        this.orderProducts = order.getOrderItems().stream()
                .map(oi -> new OrderProduct(oi.getProductVariantId(), oi.getQuantity())).toList();
        this.orderRequest = orderRequest;
    }
}
