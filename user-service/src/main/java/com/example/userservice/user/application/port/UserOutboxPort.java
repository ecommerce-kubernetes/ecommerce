package com.example.userservice.user.application.port;

public interface UserOutboxPort {
    void recordPointDeduct(Long sagaId, Long executionId);
    void recordPointRefund(Long sagaId, Long executionId);
}
