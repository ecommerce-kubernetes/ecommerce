package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.util.IdGenerator;
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
    private final IdGenerator idGenerator;

    public void addPoint(Long userId, Long referenceId, Money addPoint) {
        User user = findByIdOrThrow(userId);
        user.addPoints(addPoint);

        PointHistory history = PointHistory.createAddHistory(idGenerator, referenceId, user, addPoint);

    }

    public void deductPoint(Long userId, Long referenceId, Money deductPoint) {

    }

    private User findByIdOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

}
