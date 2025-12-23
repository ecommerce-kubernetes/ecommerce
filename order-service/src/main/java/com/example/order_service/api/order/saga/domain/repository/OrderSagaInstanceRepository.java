package com.example.order_service.api.order.saga.domain.repository;

import com.example.order_service.api.order.saga.domain.model.OrderSagaInstance;
import com.example.order_service.api.order.saga.domain.model.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderSagaInstanceRepository extends JpaRepository<OrderSagaInstance, Long> {

    @Query("select os from OrderSagaInstance os where os.startedAt < :startedAt and os.sagaStatus = :sagaStatus")
    List<OrderSagaInstance> findByStartedAtBeforeAndSagaStatus(@Param("startedAt") LocalDateTime startedAt,
                                                               @Param("sagaStatus") SagaStatus status);
}
