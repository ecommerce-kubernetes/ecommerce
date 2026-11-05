package com.example.userservice.outbox.adapter.in.scheduler;

import com.example.userservice.outbox.application.service.OutboxMessagePublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class OutboxSweepSchedulerTest {

    @InjectMocks
    private OutboxSweepScheduler outboxSweepScheduler;

    @Mock
    private OutboxMessagePublisher publisher;

    @Test
    @DisplayName("스케줄이 실행되면 좀비 메시지를 스윕한다.")
    void sendPendingOutboxMessage() {
        //when
        outboxSweepScheduler.sendPendingOutboxMessage();
        //then
        then(publisher).should().sweepZombieMessages();
    }
}
