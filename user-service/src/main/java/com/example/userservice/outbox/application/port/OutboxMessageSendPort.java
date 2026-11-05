package com.example.userservice.outbox.application.port;

import com.example.userservice.outbox.application.service.dto.OutboxMessageResult;

public interface OutboxMessageSendPort {
    void send(OutboxMessageResult result);
}
