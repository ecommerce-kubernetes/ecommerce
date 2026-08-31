package com.example.userservice.user.application.port;

public interface UserOutboxPort {
    void recordForwardSuccess(Long sagaId, Long executionId);
    void recordCompensateSuccess(Long sagaId, Long executionId);
    void recordForwardFail(Long sagaId, Long executionId, String reason);
    void recordCompensateFail(Long sagaId, Long executionId, String reason);
}
