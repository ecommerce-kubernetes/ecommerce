package com.example.order_service.service;

import com.example.order_service.dto.KafkaOrderDto;
import com.example.order_service.dto.KafkaOrderItemDto;
import com.example.order_service.dto.client.ProductRequestIdsDto;
import com.example.order_service.dto.client.CompactProductResponseDto;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.request.OrderRequest;
import com.example.order_service.dto.response.OrderItemResponseDto;
import com.example.order_service.dto.response.OrderResponseDto;
import com.example.order_service.dto.response.PageDto;
import com.example.order_service.entity.OrderItems;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService{

    private final ProductClientService productClientService;
    private final KafkaProducer kafkaProducer;
    private final OrdersRepository ordersRepository;

    @Transactional
    @Override
    public OrderResponseDto saveOrder(Long userId, OrderRequest orderRequest) {
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
//        List<OrderItemResponseDto> orderItemResponseDtoList = savedOrderItems.stream().map(orderItem -> new OrderItemResponseDto(
//                orderItem.getProductId(),
//                orderItem.getProductName(),
//                orderItem.getQuantity(),
//                orderItem.getPrice(),
//                orderItem.getMainImgUrl()
//        )).toList();
//
//        return new OrderResponseDto(
//                savedOrder.getId(),
//                savedOrder.getUserId(),
//                orderItemResponseDtoList,
//                savedOrder.getDeliveryAddress(),
//                savedOrder.getTotalPrice(),
//                savedOrder.getStatus(),
//                savedOrder.getCreateAt());
        return null;
    }

    @Override
    public PageDto<OrderResponseDto> getOrderList(Pageable pageable, Long userId, Integer year, String keyword) {
        Page<Orders> findResult = ordersRepository.findAllByParameter(pageable, userId, year, keyword);

        List<OrderResponseDto> orderResponseDtoList = findResult.getContent().stream().map(order -> {
            List<OrderItems> orderItems = order.getOrderItems();
            List<OrderItemResponseDto> orderItemResponseDtoList = orderItems.stream()
                    .map(orderItem ->
                            new OrderItemResponseDto(
                                    orderItem.getProductId(),
                                    orderItem.getProductName(),
                                    orderItem.getQuantity(),
                                    orderItem.getPrice(),
                                    orderItem.getMainImgUrl())
                    ).toList();

            return new OrderResponseDto(
                    order.getId(),
                    order.getUserId(),
                    orderItemResponseDtoList,
                    order.getDeliveryAddress(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    order.getCreateAt());
        }).toList();

        return new PageDto<>(
                orderResponseDtoList,
                pageable.getPageNumber(),
                findResult.getTotalPages(),
                pageable.getPageSize(),
                findResult.getTotalElements()
        );
    }

    @Override
    @Transactional
    public void changeOrderStatus(Long orderId, String status) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Not Found Order"));
        order.setStatus(status);
    }

}
