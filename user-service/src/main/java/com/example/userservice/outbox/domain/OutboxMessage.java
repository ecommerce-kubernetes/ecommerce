package com.example.userservice.outbox.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {

    @Id
    private Long id;

    private String topic;

    private Long sagaId;

    private MessageCommandType commandType;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private MessageResult result;

    private String failureReason;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
}
