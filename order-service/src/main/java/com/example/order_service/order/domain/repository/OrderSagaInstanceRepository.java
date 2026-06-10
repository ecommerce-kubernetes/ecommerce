package com.example.order_service.order.domain.repository;

import com.example.order_service.order.domain.saga.OrderSagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderSagaInstanceRepository extends JpaRepository<OrderSagaInstance, Long> {
    Optional<OrderSagaInstance> findByOrderNo(String orderNo);

    @Query("select s from OrderSagaInstance s join fetch s.histories where s.orderNo = :orderNo")
    Optional<OrderSagaInstance> findByOrderNoWithHistories(@Param("orderNo") String orderNo);
}
