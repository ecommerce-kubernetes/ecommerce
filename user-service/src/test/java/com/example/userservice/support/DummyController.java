package com.example.userservice.support;

import com.example.userservice.common.exception.BusinessException;
import com.example.userservice.user.exception.UserErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class DummyController {

    @GetMapping("/exception")
    public String throwBusinessException() {
        throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    @PostMapping("/not-readable")
    public String throwNotReadableException(@RequestBody TestRequestBody requestBody) {
        return "OK";
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class TestRequestBody {
        private LocalDateTime datetime;
        private Long number;
    }
}
