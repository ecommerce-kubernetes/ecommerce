package com.example.product_service.api.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test-mock")
@SpringBootTest
public abstract class ExcludeInfraTest {
    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;
}
