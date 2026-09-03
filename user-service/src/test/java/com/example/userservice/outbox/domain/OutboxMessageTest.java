package com.example.userservice.outbox.domain;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import com.example.userservice.outbox.exception.OutboxErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboxMessageTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("아웃박스 메시지를 생성한다.")
    void create() {
        //given
        CreateOutboxMessageContext context = aContext();
        //when
        OutboxMessage outboxMessage = OutboxMessage.create(context);
        //then
        assertThat(outboxMessage.getId()).isEqualTo(context.id());
        assertThat(outboxMessage.getTopic()).isEqualTo("user-saga-command");
        assertThat(outboxMessage.getRoutingKey()).isEqualTo("1");
        assertThat(outboxMessage.getHeaders()).isEqualTo("{\"X-Command-Type\":\"DEDUCT_POINT\"}");
        assertThat(outboxMessage.getPayload()).isEqualTo("{\"executionId\":1,\"userId\":1,\"usedPoints\":1000}");
        assertThat(outboxMessage.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("아웃박스 메시지의 상태를 SENT로 변경한다.")
    void sent() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        //when
        outboxMessage.sent();
        //then
        assertThat(outboxMessage.getStatus()).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    @DisplayName("아웃박스 메시지 상태를 SENT로 변경할때 아웃박스 메시지 상태가 PENDING이 아니면 예외가 발생한다")
    void sent_whenStatusNotPending_thenThrownException() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        outboxMessage.sent();
        //when
        //then
        assertThatThrownBy(outboxMessage::sent)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OutboxErrorCode.INVALID_OUTBOX_MESSAGE_STATUS);
    }

    private CreateOutboxMessageContext aContext() {
        return CreateOutboxMessageContext.builder()
                .id(idGenerator.generate())
                .topic("user-saga-command")
                .routingKey("1")
                .headers("{\"X-Command-Type\":\"DEDUCT_POINT\"}")
                .payload("{\"executionId\":1,\"userId\":1,\"usedPoints\":1000}")
                .build();
    }
}
