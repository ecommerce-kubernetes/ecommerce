package com.example.userservice.api.support;

import com.example.userservice.api.common.exception.BusinessException;
import com.example.userservice.api.common.exception.UserErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    @GetMapping("/exception")
    public String throwBusinessException() {
        throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }
}
