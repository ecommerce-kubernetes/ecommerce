package com.example.userservice.outbox.adapter.in.scheduler;

import com.example.userservice.outbox.application.service.OutboxMessagePublisher;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxMessagePublisher publisher;

    @Scheduled(cron = "0 0/3 * * * *")
    @SchedulerLock(
            name = "outbox_scheduler_lock",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT2M"
    )
    public void sendPendingOutboxMessage() {
        publisher.sendPendingMessage();
    }
}
