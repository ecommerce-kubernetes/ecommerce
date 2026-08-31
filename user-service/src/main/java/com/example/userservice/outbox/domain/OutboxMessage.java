package com.example.userservice.outbox.domain;

import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {

    @Id
    private Long id;

    private String topic;

    private String routingKey;

    private String headers;

    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Builder(access = AccessLevel.PRIVATE)
    private OutboxMessage(Long id, String topic, String routingKey, String headers, String payload, OutboxStatus status) {
        Assert.notNull(id, "아웃박스 메시지 아이디는 필수이다");
        Assert.hasText(topic, "아웃박스 메시지 토픽은 필수이다");
        Assert.hasText(routingKey, "아웃박스 메시지 토픽 라우팅 키는 필수이다");
        Assert.hasText(headers, "아웃박스 메시지 헤더는 필수이다");
        Assert.hasText(payload, "아웃박스 메시지 페이로드는 필수이다");
        Assert.notNull(status, "아웃박스 메시지 상태는 필수이다");

        this.id = id;
        this.topic = topic;
        this.routingKey = routingKey;
        this.headers = headers;
        this.payload = payload;
        this.status = status;
    }

    public static OutboxMessage create(CreateOutboxMessageContext context, IdGenerator idGenerator) {
        Long id = idGenerator.generate();

        return OutboxMessage.builder()
                .id(id)
                .topic(context.topic())
                .routingKey(context.routingKey())
                .headers(context.headers())
                .payload(context.payload())
                .status(OutboxStatus.PENDING)
                .build();
    }
}
