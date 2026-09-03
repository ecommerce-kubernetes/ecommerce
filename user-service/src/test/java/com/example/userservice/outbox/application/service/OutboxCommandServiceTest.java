package com.example.userservice.outbox.application.service;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.outbox.application.port.OutboxRepository;
import com.example.userservice.outbox.application.service.dto.event.OutboxCreatedEvent;
import com.example.userservice.outbox.domain.OutboxFixtureBuilder;
import com.example.userservice.outbox.domain.OutboxMessage;
import com.example.userservice.outbox.domain.OutboxStatus;
import com.example.userservice.outbox.domain.context.CreateOutboxMessageContext;
import com.example.userservice.outbox.exception.OutboxErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OutboxCommandServiceTest {

    @InjectMocks
    private OutboxCommandService outboxCommandService;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<OutboxMessage> outboxMessageCaptor;

    @Captor
    private ArgumentCaptor<OutboxCreatedEvent> outboxCreatedEventCaptor;

    @Test
    @DisplayName("아웃박스 메시지를 생성하여 저장하고 생성 이벤트를 발행한다.")
    void createOutbox() {
        //given
        given(idGenerator.generate()).willReturn(1L);
        given(outboxRepository.save(any(OutboxMessage.class))).willAnswer(invocation -> invocation.getArgument(0));
        CreateOutboxMessageContext context = CreateOutboxMessageContext.builder()
                .topic("order.saga.reply")
                .routingKey("1")
                .headers("{\"X-Reply-Type\":\"FORWARD\"}")
                .payload("{\"executionId\":1,\"result\":\"SUCCESS\"}")
                .build();
        //when
        outboxCommandService.createOutbox(context);
        //then
        then(outboxRepository).should().save(outboxMessageCaptor.capture());

        OutboxMessage savedMessage = outboxMessageCaptor.getValue();
        assertThat(savedMessage.getId()).isEqualTo(1L);
        assertThat(savedMessage.getTopic()).isEqualTo(context.topic());
        assertThat(savedMessage.getRoutingKey()).isEqualTo(context.routingKey());
        assertThat(savedMessage.getHeaders()).isEqualTo(context.headers());
        assertThat(savedMessage.getPayload()).isEqualTo(context.payload());
        assertThat(savedMessage.getStatus()).isEqualTo(OutboxStatus.PENDING);

        then(eventPublisher).should().publishEvent(outboxCreatedEventCaptor.capture());
        assertThat(outboxCreatedEventCaptor.getValue().outboxId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("아웃박스 메시지를 SENT로 변경한다.")
    void changeSent() {
        //given
        OutboxMessage outboxMessage = OutboxFixtureBuilder.given().build();
        given(outboxRepository.findById(outboxMessage.getId())).willReturn(Optional.of(outboxMessage));
        //when
        outboxCommandService.changeSent(outboxMessage.getId());
        //then
        assertThat(outboxMessage.getStatus()).isEqualTo(OutboxStatus.SENT);
    }

    @Test
    @DisplayName("아웃박스 메시지를 찾을 수 없으면 예외가 발생한다.")
    void changeSent_whenOutboxNotFound_thenThrownException() {
        //given
        given(outboxRepository.findById(999L)).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> outboxCommandService.changeSent(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OutboxErrorCode.OUTBOX_NOT_FOUND);
    }
}
