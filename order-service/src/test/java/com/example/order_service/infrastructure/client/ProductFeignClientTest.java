package com.example.order_service.infrastructure.client;

import com.example.order_service.api.support.ExcludeInfraTest;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;


@AutoConfigureWireMock(port = 0)
class ProductFeignClientTest extends ExcludeInfraTest {

    @Autowired
    private ProductAdaptor adaptor;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

}