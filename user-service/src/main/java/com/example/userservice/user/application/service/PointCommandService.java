package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.user.application.port.PointHistoryRepository;
import com.example.userservice.user.application.port.UserRepository;
import com.example.userservice.user.domain.PointHistory;
import com.example.userservice.user.domain.User;
import com.example.userservice.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PointCommandService {

    private final UserRepository userRepository;

    private final PointHistoryRepository pointHistoryRepository;

    private final IdGenerator idGenerator;

    public void addPoint(Long userId, Long referenceId, Money addPoint) {
        if (pointHistoryRepository.existsByReferenceId(referenceId)) {
            return;
        }

        User user = findByIdOrThrow(userId);
        user.addPoints(addPoint);

        PointHistory history = PointHistory.createAddHistory(idGenerator, referenceId, user, addPoint);
        pointHistoryRepository.save(history);
    }

    public void deductPoint(Long userId, Long referenceId, Money deductPoint) {
        if (pointHistoryRepository.existsByReferenceId(referenceId)) {
            return;
        }

        User user = findByIdOrThrow(userId);
        user.deductPoints(deductPoint);

        PointHistory history = PointHistory.createDeductHistory(idGenerator, referenceId, user, deductPoint);
        pointHistoryRepository.save(history);
    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

}
