package com.example.payment_service.controller;

import com.example.common.OrderCreatedEvent;
import com.example.common.Product;
import com.example.payment_service.dto.TestEvent;
import com.example.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_NAME = "order.created";

    public PaymentController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrderEvent(@RequestBody TestEvent event) {

        List<Product> productList = new ArrayList<>();
        Product product1 = new Product(1L, 1, 10000);
        productList.add(product1);
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .userCouponId(event.getUserCouponId())
                .productList(productList)
                .pointUsage(event.isPointUsage())
                .reservedCacheAmount(event.getReservedCacheAmount())
                .reservedPointAmount(event.getReservedPointAmount())
                .expectTotalAmount(event.getExpectTotalAmount())
                .build();

        kafkaTemplate.send(TOPIC_NAME, String.valueOf(event.getOrderId()), orderCreatedEvent);
        return ResponseEntity.ok("Order event sent to Kafka");
    }

}
