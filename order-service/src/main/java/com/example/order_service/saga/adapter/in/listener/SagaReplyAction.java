package com.example.order_service.saga.adapter.in.listener;

import com.example.order_service.saga.adapter.in.listener.dto.SagaReplyResult;
import com.example.order_service.saga.adapter.in.listener.dto.SagaReplyType;

public enum SagaReplyAction {
    FORWARD_SUCCESS,
    FORWARD_FAIL,
    COMPENSATE_SUCCESS,
    COMPENSATE_FAIL;

    public static SagaReplyAction route(SagaReplyType replyType, SagaReplyResult result) {
        if (replyType == SagaReplyType.FORWARD && result == SagaReplyResult.SUCCESS) return FORWARD_SUCCESS;
        if (replyType == SagaReplyType.COMPENSATE && result == SagaReplyResult.SUCCESS) return COMPENSATE_SUCCESS;

        if (replyType == SagaReplyType.FORWARD && result == SagaReplyResult.FAIL) return FORWARD_FAIL;
        if (replyType == SagaReplyType.COMPENSATE && result == SagaReplyResult.FAIL) return COMPENSATE_FAIL;

        throw new IllegalArgumentException("잘못된 사가 응답입니다.");
    }
}
