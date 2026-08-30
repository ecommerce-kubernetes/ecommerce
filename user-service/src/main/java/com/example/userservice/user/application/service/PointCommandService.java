package com.example.userservice.user.application.service;

import com.example.userservice.common.domain.vo.Money;
import com.example.userservice.user.application.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointCommandService {

    private final UserRepository userRepository;

    public void deductPoint(Long userId, Long referenceId, Money point) {

    }
}
