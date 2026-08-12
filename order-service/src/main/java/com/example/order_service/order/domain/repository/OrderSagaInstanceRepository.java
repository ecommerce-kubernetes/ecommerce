package com.example.order_service.order.domain.repository;

import com.example.order_service.saga.domain.OrderSagaInstance;
import com.example.order_service.saga.domain.SagaStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderSagaInstanceRepository extends JpaRepository<OrderSagaInstance, Long> {
    Optional<OrderSagaInstance> findByOrderNo(String orderNo);

    @Query("select s from OrderSagaInstance s join fetch s.histories where s.orderNo = :orderNo")
    Optional<OrderSagaInstance> findByOrderNoWithHistories(@Param("orderNo") String orderNo);

    @Query("SELECT s FROM OrderSagaInstance s " +
            "WHERE s.status IN :statuses " +
            "AND s.updatedAt <= :threshold " +
            "ORDER BY s.updatedAt ASC")
    List<OrderSagaInstance> findTimeoutSagas(
            @Param("statuses") List<SagaStatus> statuses,
            @Param("threshold") LocalDateTime threshold,
            Pageable pageable
    );
}
