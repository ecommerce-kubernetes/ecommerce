package com.example.order_service.saga.adapter.out.message.processor.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SagaCommandType {

    REDUCE_INVENTORY,
    RESTORE_INVENTORY,

    USE_COUPON,
    RESTORE_COUPON,

    USE_POINT
}
