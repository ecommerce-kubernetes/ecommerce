package com.example.order_service.order.application.service.order;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.port.dto.result.OrderCouponResult;
import com.example.order_service.order.application.port.dto.result.OrderProductsResult;
import com.example.order_service.order.application.port.dto.result.OrderUserResult;
import com.example.order_service.order.application.service.order.dto.command.OrderCreateCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.result.OrderResultDeprecated;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.adaptor.client.OrderCouponAdaptor;
import com.example.order_service.order.infrastructure.adaptor.client.OrderProductAdaptor;
import com.example.order_service.order.infrastructure.adaptor.client.OrderUserAdaptor;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
