package com.example.order_service.api.notification.controller;

import com.example.order_service.api.common.security.model.UserPrincipal;
import com.example.order_service.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/subscribe")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserPrincipal userPrincipal){
        return notificationService.createEmitter(userPrincipal.getUserId());
    }
}
