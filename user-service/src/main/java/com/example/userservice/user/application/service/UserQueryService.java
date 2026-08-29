package com.example.userservice.user.application.service;

import com.example.userservice.user.application.service.dto.result.EmailAvailableResult;
import com.example.userservice.user.application.service.dto.result.UserBalanceResult;
import com.example.userservice.user.application.service.dto.result.UserIdentityResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    public UserIdentityResult authenticate(String email, String password){
        return null;
    }

    public UserProfileResult getUserProfile(Long userId) {
        return null;
    }

    public EmailAvailableResult checkAvailableEmail(String email) {
        return null;
    }

    public UserBalanceResult getUserPoints(Long userId) {
        return null;
    }

}
