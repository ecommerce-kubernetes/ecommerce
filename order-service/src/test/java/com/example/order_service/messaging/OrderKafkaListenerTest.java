package com.example.order_service.messaging;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;



@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"order.created"})
class OrderKafkaListenerTest {

}