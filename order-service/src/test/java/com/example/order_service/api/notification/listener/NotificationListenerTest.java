package com.example.order_service.api.notification.listener;

import com.example.order_service.api.notification.service.NotificationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @InjectMocks
    private NotificationListener notificationListener;
    @Mock
    private NotificationService notificationService;

    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

}