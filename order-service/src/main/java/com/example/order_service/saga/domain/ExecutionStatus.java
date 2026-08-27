package com.example.order_service.saga.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExecutionStatus {
    PENDING, SUCCESS, FAIL
}
