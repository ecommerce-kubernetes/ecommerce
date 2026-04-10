package com.example.userservice.api.user.saga.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedSagaEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sagaId;
    private String commandType;
    private LocalDateTime processedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private ProcessedSagaEvent(Long sagaId, String commandType, LocalDateTime processedAt) {
        this.sagaId = sagaId;
        this.commandType = commandType;
        this.processedAt = processedAt;
    }

    public static ProcessedSagaEvent create(Long sagaId, String commandType) {
        return ProcessedSagaEvent.builder()
                .sagaId(sagaId)
                .commandType(commandType)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
