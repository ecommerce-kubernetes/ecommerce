package com.example.userservice.user.adapter.out.client.dto;

import lombok.Builder;

@Builder
public record PointSagaReplyPayload(
        Long executionId,
        PointSagaReplyResult result,
        String failureReason
) {
    public static PointSagaReplyPayload success(Long executionId) {
        return PointSagaReplyPayload.builder()
                .executionId(executionId)
                .result(PointSagaReplyResult.SUCCESS)
                .build();
    }

    public static PointSagaReplyPayload fail(Long executionId, String failureReason) {
        return PointSagaReplyPayload.builder()
                .executionId(executionId)
                .result(PointSagaReplyResult.FAIL)
                .failureReason(failureReason)
                .build();
    }
}
