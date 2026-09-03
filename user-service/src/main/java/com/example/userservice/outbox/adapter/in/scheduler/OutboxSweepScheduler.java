package com.example.userservice.outbox.adapter.in.scheduler;

import com.example.userservice.outbox.application.service.OutboxMessagePublisher;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxSweepScheduler {

    private final OutboxMessagePublisher publisher;

    @Scheduled(fixedDelayString = "${outbox.scheduler.delay}")
    @SchedulerLock(
            name = "outbox_scheduler_lock",
            lockAtLeastFor = "PT30S",
            lockAtMostFor = "PT3M"
    )
    public void sendPendingOutboxMessage() {
        publisher.sweepZombieMessages();
    }
}
