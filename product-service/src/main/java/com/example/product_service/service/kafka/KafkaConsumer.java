package com.example.product_service.service.kafka;

import com.example.product_service.dto.KafkaOrderDto;
import com.example.product_service.dto.KafkaOrderItemDto;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class KafkaConsumer {

    private ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "decrement_product", groupId = "products")
    public void decrementProductListen(ConsumerRecord<String, Object> record){
        KafkaOrderDto kafkaOrderDto;
        try{
            kafkaOrderDto = mapper.readValue(record.value().toString(), KafkaOrderDto.class);
        }
        catch (JacksonException e){
            throw new RuntimeException(e);
        }

        List<KafkaOrderItemDto> orderItemDtoList = kafkaOrderDto.getOrderItemDtoList();
        for (KafkaOrderItemDto kafkaOrderItemDto : orderItemDtoList) {
            log.info("{}", kafkaOrderItemDto.getProductId());
        }

    }
}
