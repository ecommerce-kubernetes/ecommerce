package com.example.order_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseConnectionService {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 60 * 1000L;

    public SseEmitter create(Long orderId){
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        if(emitters.containsKey(orderId)){
            emitters.remove(orderId);
        }

        emitters.put(orderId, emitter);
        emitter.onCompletion(() -> {
            emitters.remove(orderId);
        });
        emitter.onTimeout(() -> {
            emitters.remove(orderId);
        });
        emitter.onError(e -> {
            emitters.remove(orderId);
        });

        send(orderId, "connected", "SSE connection establish");

        return emitter;
    }

    public void send(Long orderId, String eventName, Object data){
        SseEmitter emitter = emitters.get(orderId);
        if(emitter != null){
            try{
                emitter.send(SseEmitter.event()
                        .id(orderId + "_" + System.currentTimeMillis())
                        .name(eventName)
                        .data(data));
            } catch (IOException e){
                emitters.remove(orderId);
            }
        } else {
            // 보낼사람 없음
        }
    }
}
