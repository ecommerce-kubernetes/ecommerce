package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.infrastructure.adaptor.client.OrderCouponAdaptor;
import com.example.order_service.order.infrastructure.adaptor.client.OrderProductAdaptor;
import com.example.order_service.order.infrastructure.adaptor.client.OrderUserAdaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OrderFacadeTest {

    @InjectMocks
    private OrderFacade orderFacade;
    @Mock
    private OrderSheetRepository orderSheetRepository;
    @Mock
    private OrderCommandService orderCommandService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderProductAdaptor orderProductAdaptor;
    @Mock
    private OrderCouponAdaptor orderCouponAdaptor;
    @Mock
    private OrderUserAdaptor orderUserAdaptor;

    @Mock
    private PointUsagePolicy pointPolicy;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));


    private OrderSheet createOrderSheet() {
        return null;
    }

    private OrderSheet createOrderSheetWithoutCoupon() {
        return null;
    }
}
