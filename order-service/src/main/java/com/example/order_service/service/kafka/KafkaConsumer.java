package com.example.order_service.service.kafka;

import com.example.order_service.service.CartService;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private ObjectMapper mapper = new ObjectMapper();
    private final OrderService orderService;
    private final CartService cartService;

    @KafkaListener(topics = "change_orders", groupId = "orders")
    public void changeOrdersListen(ConsumerRecord<String, Object> record){

    }

    @KafkaListener(topics = "delete_product", groupId = "orders")
    public void deleteCartItemForDeletedProduct(ConsumerRecord<String, Object> record){
        
    }
}
