package com.example.order_service.order.infrastructure.adaptor.persistence;

import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.repository.OrderSearchRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Repository
public class OrderQueryDslRepository implements OrderSearchRepository {

    public OrderQueryDslRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> searchOrders(Long userId, OrderSearchCommand command, Pageable pageable) {
        return null;
    }


    @Override
    public List<Order> findOrdersBefore(LocalDateTime threshold, int size) {
        return null;
    }
}
