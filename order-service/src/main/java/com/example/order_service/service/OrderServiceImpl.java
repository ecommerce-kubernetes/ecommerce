package com.example.order_service.service;

import com.example.order_service.dto.KafkaOrderDto;
import com.example.order_service.dto.KafkaOrderItemDto;
import com.example.order_service.dto.client.ProductResponseDto;
import com.example.order_service.dto.request.OrderItemRequestDto;
import com.example.order_service.dto.request.OrderRequestDto;
import com.example.order_service.dto.response.OrderItemResponseDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.repository.OrderItemsRepository;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService{

    private final ProductClientService productClientService;
    private final KafkaProducer kafkaProducer;
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;

    @Transactional
    @Override
    public OrderResponseDto saveOrder(Long userId, OrderRequestDto orderRequestDto) {
        List<OrderItemRequestDto> orderItemRequest = orderRequestDto.getItems();

        List<AbstractMap.SimpleEntry<ProductResponseDto, OrderItemRequestDto>> orderItemRequestMap =
                orderItemRequest.stream().map(orderItemRequestDto ->
                        new AbstractMap.SimpleEntry<>(
                            productClientService.fetchProduct(orderItemRequestDto.getProductId()),
                            orderItemRequestDto
                        )
                ).toList();

        int totalPrice = orderItemRequestMap.stream()
                .mapToInt(entry -> entry.getKey().getPrice() * entry.getValue().getQuantity()).sum();

        Orders order = new Orders(userId, totalPrice, "PENDING", orderRequestDto.getDeliveryAddress());
        Orders savedOrder = ordersRepository.save(order);

        List<OrderItems> orderItems = orderItemRequestMap.stream()
                .map(entry -> new OrderItems(
                        savedOrder,
                        entry.getKey().getId(),
                        entry.getKey().getName(),
                        entry.getKey().getPrice(),
                        entry.getValue().getQuantity()
                )).toList();

        List<OrderItems> savedOrderItems = orderItemsRepository.saveAll(orderItems);

        List<KafkaOrderItemDto> kafkaOrderItems = savedOrderItems.stream().map(orderItem ->
                new KafkaOrderItemDto(orderItem.getProductId(), orderItem.getQuantity())).toList();

        kafkaProducer.sendMessage("decrement_product", new KafkaOrderDto(savedOrder.getId(), kafkaOrderItems));

        List<OrderItemResponseDto> orderItemResponseDtoList = savedOrderItems.stream().map(orderItem -> new OrderItemResponseDto(
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        )).toList();

        return new OrderResponseDto(
                savedOrder.getId(),
                savedOrder.getUserId(),
                orderItemResponseDtoList,
                savedOrder.getDeliveryAddress(),
                savedOrder.getTotalPrice(),
                savedOrder.getStatus());
    }
}
