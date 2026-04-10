package com.example.product_service.api.product.saga.domain.repository;

import com.example.product_service.api.product.saga.domain.model.ProcessedSagaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedSagaEventRepository extends JpaRepository<ProcessedSagaEvent, Long> {
    boolean existsBySagaIdAndCommandType(Long sagaId, String commandType);
}
