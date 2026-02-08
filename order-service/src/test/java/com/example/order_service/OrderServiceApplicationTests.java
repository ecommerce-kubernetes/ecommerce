package com.example.order_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(
		partitions = 1,
		brokerProperties = { "listeners=PLAINTEXT://127.0.0.1:0" },
		ports = { 0 }
)
@TestPropertySource(properties = {
		"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
})
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
