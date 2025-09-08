package com.example.order_service.service;

import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.PageDto;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final ProductClientService productClientService;
    private final KafkaProducer kafkaProducer;
    private final OrdersRepository ordersRepository;

    @Transactional
    public OrderResponse saveOrder(Long userId, OrderRequest orderRequest) {
//        List<OrderItemRequest> orderItemRequest = orderRequest.getItems();
//
//        List<Long> ids = orderItemRequest.stream().map(
//                OrderItemRequest::getProductId
//        ).toList();
//        ProductRequestIdsDto productRequestIdsDto = new ProductRequestIdsDto(ids);
//
//        List<CompactProductResponseDto> products = productClientService.fetchProductBatch(productRequestIdsDto);
//
//        Map<Long, CompactProductResponseDto> productMap = products.stream()
//                .collect(Collectors.toMap(CompactProductResponseDto::getId, Function.identity()));
//
//        List<AbstractMap.SimpleEntry<CompactProductResponseDto, OrderItemRequest>> orderItemRequestMap =
//                orderItemRequest.stream()
//                        .map(item -> new AbstractMap.SimpleEntry<>(
//                                productMap.get(item.getProductId()),
//                                item
//                        ))
//                        .toList();
//
//        int totalPrice = orderItemRequestMap.stream()
//                .mapToInt(entry -> entry.getKey().getPrice() * entry.getValue().getQuantity()).sum();
//
//        Orders order = new Orders(userId, totalPrice, "PENDING", orderRequest.getDeliveryAddress());
//        Orders savedOrder = ordersRepository.save(order);
//
//
//        orderItemRequestMap.forEach(entry -> new OrderItems(
//                savedOrder,
//                entry.getKey().getId(),
//                entry.getKey().getName(),
//                entry.getKey().getPrice(),
//                entry.getValue().getQuantity(),
//                entry.getKey().getMainImgUrl()
//        ));
//
//        List<OrderItems> savedOrderItems = savedOrder.getOrderItems();
//
//        List<KafkaOrderItemDto> kafkaOrderItems = savedOrderItems.stream().map(orderItem ->
//                new KafkaOrderItemDto(orderItem.getProductId(), orderItem.getQuantity())).toList();
//
//        kafkaProducer.sendMessage("decrement_product", new KafkaOrderDto(savedOrder.getId(), kafkaOrderItems));
//
//        List<OrderItemResponse> orderItemResponseDtoList = savedOrderItems.stream().map(orderItem -> new OrderItemResponse(
//                orderItem.getProductId(),
//                orderItem.getProductName(),
//                orderItem.getQuantity(),
//                orderItem.getPrice(),
//                orderItem.getMainImgUrl()
//        )).toList();
//
//        return new OrderResponse(
//                savedOrder.getId(),
//                savedOrder.getUserId(),
//                orderItemResponseDtoList,
//                savedOrder.getDeliveryAddress(),
//                savedOrder.getTotalPrice(),
//                savedOrder.getStatus(),
//                savedOrder.getCreateAt());
        return null;
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
