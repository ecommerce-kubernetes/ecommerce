package com.example.product_service.service.kafka;

import com.example.product_service.dto.KafkaOrderChangeDto;
import com.example.product_service.dto.KafkaOrderDto;
import com.example.product_service.dto.KafkaOrderItemDto;
import com.example.product_service.exception.InsufficientStockException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private ObjectMapper mapper = new ObjectMapper();
    private final KafkaProducer kafkaProducer;

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
        KafkaOrderChangeDto kafkaOrderChangeDto;
        try{
//            productService.decrementStockQuantity(orderItemDtoList);
            kafkaOrderChangeDto = new KafkaOrderChangeDto(kafkaOrderDto.getId(), "SUCCESS", "DECREMENT SUCCESS");
        } catch (InsufficientStockException ex){
            kafkaOrderChangeDto = new KafkaOrderChangeDto(kafkaOrderDto.getId(), "FAIL", ex.getMessage());
        }
        kafkaProducer.sendMessage("change_orders", kafkaOrderChangeDto);
    }
}
