package com.example.userservice.outbox.adapter.in.listener;

import com.example.userservice.outbox.application.service.OutboxMessagePublisher;
import com.example.userservice.outbox.application.service.dto.event.OutboxCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OutboxEventListenerTest {

    @InjectMocks
    private OutboxEventListener outboxEventListener;

    @Mock
    private OutboxMessagePublisher publisher;

    @Test
    @DisplayName("아웃박스 생성 이벤트를 수신하면 해당 아웃박스 메시지를 발행한다.")
    void handleOutboxCreatedEvent() {
        //given
        OutboxCreatedEvent event = new OutboxCreatedEvent(1L);
        //when
        outboxEventListener.handleOutboxCreatedEvent(event);
        //then
        then(publisher).should().publishMessage(1L);
    }
}
