package com.example.order_service.service;

import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.CreateOrderResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final ProductClientService productClientService;
    private final KafkaProducer kafkaProducer;
    private final OrdersRepository ordersRepository;

    @Transactional
    public CreateOrderResponse saveOrder(Long userId, OrderRequest request) {
        Orders order = new Orders(userId, "PENDING", request.getDeliveryAddress());
        List<OrderItems> orderItems = request.getItems().stream().map(item -> new OrderItems(item.getProductVariantId(), item.getQuantity()))
                .toList();
        order.addOrderItems(orderItems);
        Orders save = ordersRepository.save(order);

        //TODO 카프카 토픽 메시지 게시
        return new CreateOrderResponse(save, "http://test.com/1/subscribe");
    }

    public PageDto<OrderResponse> getOrderList(Pageable pageable, Long userId, String year, String keyword) {
        return null;
    }

    @Transactional
    public void changeOrderStatus(Long orderId, String status) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Not Found Order"));
        order.setStatus(status);
    }

}
