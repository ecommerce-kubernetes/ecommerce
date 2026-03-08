package com.example.product_service.api.product.saga.service;

import com.example.common.product.ProductCommandType;
import com.example.common.product.ProductSagaCommand;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.product.saga.producer.SagaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaProcessor {
    private final ProductSagaCommandExecutor executor;
    private final SagaEventProducer sagaEventProducer;

    public void productSagaProcess(ProductSagaCommand command) {
        try {
            boolean isAlreadyProcessed = executor.processSagaCommand(command);
            if (isAlreadyProcessed) {
                log.info("이미 처리된 Saga Command");
            }
            sagaEventProducer.sendSagaSuccess(command.getSagaId(), command.getOrderNo());
        } catch (BusinessException e) {
            handleException(command, e.getErrorCode().name(), e.getMessage());
        } catch (Exception e) {
            handleException(command, "SYSTEM_ERROR", "시스템 오류");
        }
    }

    private void handleException(ProductSagaCommand command, String code, String message) {
        if (command.getType() == ProductCommandType.RESTORE_STOCK) {
            log.error("🚨 재고 복구 실패! 재시도 필요. SagaID: {}", command.getSagaId());
            throw new RuntimeException("재고 복구 실패 - 재시도 요망");
        }

        log.warn("재고 차감 실패. SagaID: {}", command.getSagaId());
        sagaEventProducer.sendSagaFailure(command.getSagaId(), command.getOrderNo(), code, message);
    }
}
