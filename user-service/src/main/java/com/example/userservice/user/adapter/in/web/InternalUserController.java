package com.example.userservice.user.adapter.in.web;

import com.example.userservice.user.adapter.in.web.dto.UserPointsResponse;
import com.example.userservice.user.adapter.in.web.dto.UserProfileResponse;
import com.example.userservice.user.application.service.UserService;
import com.example.userservice.user.application.service.dto.result.UserPointsResult;
import com.example.userservice.user.application.service.dto.result.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable("userId") Long userId) {
        UserProfileResult result = userService.getUserProfile(userId);
        UserProfileResponse response = UserProfileResponse.from(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/points")
    public ResponseEntity<UserPointsResponse> getUserPoints(@PathVariable("userId") Long userId) {
        UserPointsResult result = userService.getUserPoints(userId);
        UserPointsResponse response = UserPointsResponse.from(result);
        return ResponseEntity.ok(response);
    }
}
