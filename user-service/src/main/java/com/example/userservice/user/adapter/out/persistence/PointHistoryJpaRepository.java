package com.example.userservice.user.adapter.out.persistence;

import com.example.userservice.user.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
}
