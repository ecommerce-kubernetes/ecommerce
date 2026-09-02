package com.example.userservice.outbox.application.service;

import com.example.userservice.outbox.application.port.OutboxMessageSendPort;
import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxMessagePublisher {

    private final OutboxQueryService outboxQueryService;

    private final OutboxCommandService outboxCommandService;

    private final OutboxMessageSendPort outboxMessageSendPort;

    public void sendPendingMessage() {
        List<OutboxMessageResult> messages = outboxQueryService.getPendingOutbox();

        if (messages.isEmpty()) {
            return;
        }

        sendMessage(messages);
    }

    private void sendMessage(List<OutboxMessageResult> messages) {
        log.info("[OutboxMessagePublisher]: 아웃박스 메시지 발행 시작");

        for (OutboxMessageResult message : messages) {
            try {
                outboxMessageSendPort.send(message);
                outboxCommandService.changeSent(message.id());
            } catch (Exception e) {
                log.error("[OutboxMessagePublisher]: 아웃박스 메시지 발행 실패 id: {}", message.id(), e);
            }
        }

        log.info("[OutboxMessagePublisher]: 아웃박스 메시지 발행 완료");
    }
}
