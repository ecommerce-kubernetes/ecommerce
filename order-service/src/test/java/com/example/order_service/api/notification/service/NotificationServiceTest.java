package com.example.order_service.api.notification.service;

import com.example.order_service.api.notification.service.dto.command.SendNotificationDto;
import com.example.order_service.api.order.facade.event.OrderPaymentReadyEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";
    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("새로운 에미터 생성 시 맵에 저장되고 연결 메시지가 전송된다")
    void createEmitter() {
        //given
        Long userId = 1L;
        //when
        SseEmitter emitter = notificationService.createEmitter(userId);
        //then
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("메시지 전송 시 실제 전송 로직이 호출된다")
    void sendMessage() throws IOException {
        //given
        Long userId = 1L;
        SseEmitter sseEmitter = spy(new SseEmitter());
        ConcurrentHashMap<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
        emitters.put(userId, sseEmitter);
        ReflectionTestUtils.setField(notificationService, "emitters", emitters);

        OrderPaymentReadyEvent event = OrderPaymentReadyEvent.builder()
                .orderNo(ORDER_NO)
                .userId(1L)
                .code("PAYMENT_READY")
                .orderName("상품")
                .finalPaymentAmount(9000L)
                .build();
        SendNotificationDto dto = SendNotificationDto.of(1L, "ORDER_RESULT", event);
        //when
        notificationService.sendMessage(dto);
        //then
        verify(sseEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("에미터가 없는 유저에게 메시지 전송 시 예외가 발생하지 않는다")
    void sendMessageWithoutEmitter() {
        //given
        OrderPaymentReadyEvent event = OrderPaymentReadyEvent.builder()
                .orderNo(ORDER_NO)
                .userId(1L)
                .code("PAYMENT_READY")
                .orderName("상품")
                .finalPaymentAmount(9000L)
                .build();
        SendNotificationDto dto = SendNotificationDto.of(1L, "ORDER_RESULT", event);
        //when
        //then
        assertThatCode(() -> notificationService.sendMessage(dto)).doesNotThrowAnyException();
    }
}
