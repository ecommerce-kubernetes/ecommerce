package com.example.userservice.support;

import com.example.userservice.auth.exception.AuthUserPortErrorCode;
import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.common.exception.PortException;
import com.example.userservice.user.exception.UserErrorCode;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@TestComponent
@RestController
public class ExceptionTestController {

    @GetMapping("/exception/business")
    public String throwBusinessException() {
        throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    @GetMapping("/exception/port")
    public String throwPortException(){
        throw new PortException(AuthUserPortErrorCode.INVALID_CREDENTIALS);
    }
}
