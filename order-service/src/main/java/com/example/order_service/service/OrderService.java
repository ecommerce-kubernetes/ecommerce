package com.example.order_service.service;

import com.example.order_service.common.MessagePath;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.dto.SuccessOrderDto;
import com.example.order_service.service.dto.SuccessOrderItemDto;
import com.example.order_service.service.event.PendingOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
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

    private String buildSubscribeUrl(Long orderId){
        return "http://test.com/" + orderId + "/subscribe";
    }

}
