package com.example.order_service.service;

import com.example.common.OrderCreatedEvent;
import com.example.common.OrderProduct;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private static final String ORDER_CREATED_TOPIC = "order.created";
    private final KafkaProducer kafkaProducer;
    private final OrdersRepository ordersRepository;

    @Transactional
    public CreateOrderResponse saveOrder(Long userId, OrderRequest request) {
        Orders order = new Orders(userId, "PENDING", request.getDeliveryAddress());
        List<OrderItems> orderItems = request.getItems().stream().map(item -> new OrderItems(item.getProductVariantId(), item.getQuantity()))
                .toList();
        order.addOrderItems(orderItems);
        Orders save = ordersRepository.save(order);

        OrderCreatedEvent orderEvent = createOrderEvent(save, request);
        kafkaProducer.sendMessage(ORDER_CREATED_TOPIC, orderEvent);
        String url = buildSubscribeUrl(save.getId());
        return new CreateOrderResponse(save, url);
    }

    public PageDto<OrderResponse> getOrderList(Pageable pageable, Long userId, String year, String keyword) {
        return null;
    }

    private OrderCreatedEvent createOrderEvent(Orders order, OrderRequest request){
        List<OrderProduct> orderProducts = order.getOrderItems().stream().map(oi ->
                        new OrderProduct(oi.getProductVariantId(), oi.getQuantity()))
                .toList();
        int useReserve = request.getUseToReserve() != null ? request.getUseToReserve() : 0;

        return new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                request.getCouponId(),
                orderProducts,
                (request.getUseToReserve() != null && request.getUseToReserve() !=0),
                useReserve,
                request.getUseToCash(),
                request.getUseToCash() + useReserve
        );
    }

    private String buildSubscribeUrl(Long orderId){
        return "http://test.com/" + orderId + "/subscribe";
    }

}
