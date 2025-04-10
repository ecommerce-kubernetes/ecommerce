package com.example.order_service.service.kafka;

import com.example.order_service.dto.KafkaOrderStatusDto;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.OrdersRepository;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private ObjectMapper mapper = new ObjectMapper();
    private final OrderService orderService;

    @KafkaListener(topics = "change_orders", groupId = "orders")
    public void changeOrdersListen(ConsumerRecord<String, Object> record){
        KafkaOrderStatusDto kafkaOrderStatusDto;
        try{
            kafkaOrderStatusDto = mapper.readValue(record.value().toString(), KafkaOrderStatusDto.class);
        } catch (JacksonException e){
            throw new RuntimeException(e);
        }

        String status = kafkaOrderStatusDto.getStatus().toUpperCase();
        orderService.changeOrderStatus(kafkaOrderStatusDto.getOrderId(), status);
    }

    //TODO 상품 서비스에서 상품이 삭제된 경우 주문서비스 장바구니에 포함된 해당 상품을 삭제해야함 추가 메서드 구현해야됨
    // 배치쿼리로 구현
}
