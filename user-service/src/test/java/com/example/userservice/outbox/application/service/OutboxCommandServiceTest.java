package com.example.userservice.outbox.application.service;

import com.example.userservice.outbox.adapter.out.persistence.OutboxJpaRepository;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import com.example.userservice.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IsolatedTest
@Transactional
class OutboxCommandServiceTest {

    @Autowired
    private OutboxCommandService outboxCommandService;

    @Autowired
    private OutboxJpaRepository outboxJpaRepository;

    @Test
    @DisplayName("아웃박스 메시지를 생성하여 저장한다.")
    void createOutbox() {
        //given
        CreateOutboxMessageContext context = CreateOutboxMessageContext.builder()
                .topic("order.saga.reply")
                .routingKey("1")
                .headers("{\"X-Reply-Type\":\"FORWARD\"}")
                .payload("{\"executionId\":1,\"result\":\"SUCCESS\"}")
                .build();
        //when
        outboxCommandService.createOutbox(context);
        //then
        List<OutboxMessage> outboxMessages = outboxJpaRepository.findAll();
        assertThat(outboxMessages).hasSize(1);

        OutboxMessage savedMessage = outboxMessages.getFirst();
        assertThat(savedMessage.getId()).isNotNull();
        assertThat(savedMessage.getTopic()).isEqualTo(context.topic());
        assertThat(savedMessage.getRoutingKey()).isEqualTo(context.routingKey());
        assertThat(savedMessage.getHeaders()).isEqualTo(context.headers());
        assertThat(savedMessage.getPayload()).isEqualTo(context.payload());
        assertThat(savedMessage.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }
}
