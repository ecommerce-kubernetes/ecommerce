package com.example.order_service.service.kafka;

import com.example.order_service.dto.KafkaOrderStatusDto;
import com.example.order_service.entity.Orders;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.OrdersRepository;
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
    private final OrdersRepository ordersRepository;
    @Transactional
    @KafkaListener(topics = "change_orders", groupId = "orders")
    public void changeOrdersListen(ConsumerRecord<String, Object> record){
        KafkaOrderStatusDto kafkaOrderStatusDto;
        try{
            kafkaOrderStatusDto = mapper.readValue(record.value().toString(), KafkaOrderStatusDto.class);
        } catch (JacksonException e){
            throw new RuntimeException(e);
        }

        Orders order = ordersRepository.findById(kafkaOrderStatusDto.getOrderId())
                .orElseThrow(() -> new NotFoundException("Not Found Order"));

        if(kafkaOrderStatusDto.getStatus().equalsIgnoreCase("SUCCESS")){
            order.setStatus("SUCCESS");
        }
        else {
            order.setStatus("CANCEL");
        }
    }
}
