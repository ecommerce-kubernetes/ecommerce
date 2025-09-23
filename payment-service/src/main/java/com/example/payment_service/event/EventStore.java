package com.example.payment_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStore {

    private final RedisTemplate<String, EventStatus> eventStatusRedisTemplate;
    private final RedisTemplate<String, Object> eventObjectsRedisTemplate;

    // 이벤트 상태를 생성하고 기본 상태(PENDING)로 설정
    public void createEvent(String eventKey, Object eventObject) {
        eventStatusRedisTemplate.opsForValue().set("event:status:" + eventKey, EventStatus.PENDING, Duration.ofMinutes(5));
        eventObjectsRedisTemplate.opsForValue().set("event:object:" + eventKey, eventObject, Duration.ofMinutes(5));
        log.info("Successfully save [key:{}] to redis ", "event:status:" + eventKey);
    }

    // 이벤트 상태를 성공으로 업데이트
    public void markEventAsSuccess(String eventKey) {
        eventStatusRedisTemplate.opsForValue().set("event:status:" + eventKey, EventStatus.SUCCESS, Duration.ofMinutes(5));
    }

    // 이벤트 상태를 실패로 업데이트
    public void markEventAsFailed(String eventKey) {
        eventStatusRedisTemplate.opsForValue().set("event:status:" + eventKey, EventStatus.FAILED, Duration.ofMinutes(5));
    }

    // 특정 이벤트 상태 조회
    public EventStatus getEventStatus(String eventKey) {

        return eventStatusRedisTemplate.opsForValue().get("event:status:" + eventKey);
    }

    // 특정 이벤트 객체 조회
    public Object getEventObject(String eventKey) {
        return eventObjectsRedisTemplate.opsForValue().get("event:object:" + eventKey);
    }

    public boolean tryMarkProcessing(String eventKey) {
        // Redis SETNX 방식: 이미 존재하면 false 반환
        Boolean result = eventStatusRedisTemplate.opsForValue()
                .setIfAbsent("event:status:" + eventKey, EventStatus.PROCESSING, Duration.ofMinutes(5)); // TTL 추가
        return Boolean.TRUE.equals(result);
    }

    public boolean tryMarkRollback(String eventKey) {
        // Redis SETNX 방식: 이미 존재하면 false 반환
        Boolean result = eventStatusRedisTemplate.opsForValue()
                .setIfAbsent("event:status:" + eventKey, EventStatus.ROLLBACK, Duration.ofMinutes(5)); // TTL 추가
        return Boolean.TRUE.equals(result);
    }

}
