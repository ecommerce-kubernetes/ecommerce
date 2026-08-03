package com.example.order_service.order.application.service.order;

import com.example.order_service.order.domain.repository.OrderRepositoryDeprecated;
import com.example.order_service.support.annotation.IsolatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@RecordApplicationEvents
@IsolatedTest
@Transactional
public class OrderQueryServiceTest {

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderRepositoryDeprecated orderRepositoryDepreCated;


}
