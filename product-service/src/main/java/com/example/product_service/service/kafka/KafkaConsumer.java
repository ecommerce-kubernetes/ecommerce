package com.example.product_service.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumer {

    @KafkaListener(topics = "test-topic", groupId = "my-group")
    public void listen(ConsumerRecord<String, Object> record){
        log.info("Received: {}" , record.value());
    }
}
