package com.example.userservice.outbox.application.service;

import com.example.userservice.common.properties.OutboxSweepProperties;
import com.example.userservice.outbox.application.port.OutboxMessageSendPort;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import com.example.userservice.outbox.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxMessagePublisher {

    private final OutboxQueryService outboxQueryService;

    private final OutboxCommandService outboxCommandService;

    private final OutboxMessageSendPort outboxMessageSendPort;

    private final OutboxSweepProperties outboxSweepProperties;

    private final Clock clock;

    public void publishMessage(Long outboxId) {
        OutboxMessageResult outbox = outboxQueryService.getOutbox(outboxId);

        if (outbox.status() != OutboxStatus.PENDING) return;

        publish(outbox);
    }

    public void sweepZombieMessages() {
        LocalDateTime threshold = LocalDateTime.now(clock).minus(outboxSweepProperties.thresholdSecond());
        List<OutboxMessageResult> zombieOutboxes = outboxQueryService.getZombieOutboxes(threshold);

        if (zombieOutboxes.isEmpty()) return;

        log.info("[OutboxMessagePublisher]: 좀비 메시지 {}건 스윕 시작", zombieOutboxes.size());
        for (OutboxMessageResult message : zombieOutboxes) {
            publish(message);
        }
    }

    private void publish(OutboxMessageResult message) {
        try {
            outboxMessageSendPort.send(message);
            outboxCommandService.changeSent(message.id());
        } catch (Exception e) {
            log.error("메시지 발행 실패. Outbox ID: {}", message.id(), e);
        }
    }
}
