package com.example.userservice.api.user.saga.service;

import com.example.common.user.UserCommandType;
import com.example.common.user.UserSagaCommand;
import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.user.saga.producer.SagaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaProcessor {
    private final UserSagaCommandExecutor executor;
    private final SagaEventProducer sagaEventProducer;

    public void userSagaProcess(UserSagaCommand command) {
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

    private void handleException(UserSagaCommand command, String code, String message) {
        if (command.getType() == UserCommandType.REFUND_POINT) {
            log.error("포인트 복구 실패! 재시도 필요. SagaID: {}", command.getSagaId());
            throw new RuntimeException("포인트 복구 실패 - 재시도 요망");
        }

        log.warn("재고 차감 실패. SagaID: {}", command.getSagaId());
        sagaEventProducer.sendSagaFailure(command.getSagaId(), command.getOrderNo(), code, message);
    }
}
