package com.example.userservice.user.application.port;

import com.example.userservice.user.domain.PointHistory;

public interface PointHistoryRepository {

    PointHistory save(PointHistory pointHistory);

    boolean existsByReferenceId(Long referenceId);
}
