package com.example.userservice.outbox.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxMessagePublisher {

    private final OutboxQueryService outboxQueryService;

    private final OutboxCommandService outboxCommandService;

}
