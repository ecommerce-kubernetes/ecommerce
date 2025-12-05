package com.example.order_service.service;

import com.example.order_service.api.cart.domain.service.CartService;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("excludeInfra")
@DataJpaTest
@Import(CartService.class)
public abstract class ExcludeInfraIntegrationTestSupport {
}
