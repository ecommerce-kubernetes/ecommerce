package com.example.order_service.api.notification.service;

import com.example.order_service.api.notification.listener.dto.OrderNotificationDto;
import com.example.order_service.api.notification.service.dto.OrderNotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 5;

    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        this.emitters.put(userId, emitter);

        initEmitter(userId, emitter);

        send(userId, "CONNECT", "CONNECTED SUCCESSFULLY");

        return emitter;
    }

    public void sendMessage(OrderNotificationDto dto) {
        OrderNotificationResponse response = OrderNotificationResponse.from(dto);
        send(response.getUserId(), "ORDER_RESULT", response);
    }

    private void initEmitter(Long userId, SseEmitter emitter) {
        emitter.onCompletion(() -> this.emitters.remove(userId, emitter));
        emitter.onTimeout(() -> this.emitters.remove(userId, emitter));
        emitter.onError((e) -> this.emitters.remove(userId, emitter));
    }

    private void send(Long userId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("SSE 전송 실패 : {}", e.getMessage());
            }
        }
    }
}
