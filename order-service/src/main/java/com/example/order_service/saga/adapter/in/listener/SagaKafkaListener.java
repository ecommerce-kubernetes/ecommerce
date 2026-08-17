package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.saga.config.SagaTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaKafkaListener {

    private final SagaTopicProperties topic;

}
