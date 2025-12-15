package com.example.order_service.api.order.saga.domain.model;

import com.example.order_service.api.order.saga.domain.model.vo.Payload;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    @Enumerated(EnumType.STRING)
    private Step step;
    @Enumerated(EnumType.STRING)
    private Progress progress;
    @Column(name = "payload", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Payload payload;
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
