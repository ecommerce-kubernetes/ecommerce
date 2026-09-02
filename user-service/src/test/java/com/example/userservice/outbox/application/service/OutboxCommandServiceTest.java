package com.example.userservice.outbox.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.outbox.adapter.out.persistence.OutboxJpaRepository;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import com.example.userservice.outbox.exception.OutboxErrorCode;
import com.example.userservice.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("아웃박스 메시지를 SENT로 변경한다.")
    void changeSent() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        outboxJpaRepository.save(outboxMessage);
        //when
        outboxCommandService.changeSent(outboxMessage.getId());
        //then
        OutboxMessage findOutbox = outboxJpaRepository.findById(outboxMessage.getId()).orElseThrow();
        assertThat(findOutbox.getStatus()).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    @DisplayName("아웃박스 메시지를 찾을 수 없으면 예외가 발생한다.")
    void changeSent_whenOutboxNotFound_thenThrownException() {
        //given
        //when
        //then
        assertThatThrownBy(() -> outboxCommandService.changeSent(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OutboxErrorCode.OUT_BOX_NOT_FOUND);
    }
}
