package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.OrderCouponPort;
import com.example.order_service.order.application.port.OrderProductPort;
import com.example.order_service.order.application.port.OrderSheetRepository;
import com.example.order_service.order.application.port.OrderUserPort;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.application.service.fixture.OrderCommandFixture;
import com.example.order_service.order.application.service.fixture.OrderCouponResultFixture;
import com.example.order_service.order.application.service.fixture.OrderProductResultFixture;
import com.example.order_service.order.application.service.fixture.OrderUserResultFixture;
import com.example.order_service.order.application.service.order.dto.command.CreateOrderCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderCreateResult;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.OrderSheetFixtureBuilder;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderFacadeTest {

    @InjectMocks
    private OrderFacade orderFacade;
    @Mock
    private OrderSheetRepository orderSheetRepository;
    @Mock
    private OrderCommandService orderCommandService;
    @Mock
    private OrderProductPort orderProductPort;
    @Mock
    private OrderCouponPort orderCouponPort;
    @Mock
    private OrderUserPort orderUserPort;
    @Spy
    private PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        OrderValidator orderValidator = new OrderValidator();
        OrderContextFactory contextFactory = new OrderContextFactory();
        orderFacade = new OrderFacade(orderSheetRepository, orderCommandService, orderProductPort, orderCouponPort, orderUserPort, pointUsagePolicy,
                orderValidator, contextFactory, clock);
    }

    @Test
    @DisplayName("주문을 생성한다.")
    void createOrder() {
        //given
        CreateOrderCommand command = OrderCommandFixture.anCreateOrderCommand().build();

        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인", couponDiscountPolicy, 1);
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "1000원 할인", couponDiscountPolicy, Money.wons(10000L));
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");

        OrderSheet orderSheet = OrderSheetFixtureBuilder.given()
                .withShippingAddress(shippingAddress)
                .withCartCoupon(cartCoupon)
                .withItemCoupon(itemCoupon)
                .withUsedPoint(Money.wons(1000L))
                .build();

        OrderProductsResult products = OrderProductResultFixture.anOrderProducts().build();

        ItemCouponsResult.ItemCouponResult itemCouponResult = OrderCouponResultFixture.anItemCoupon().itemCoupon(itemCoupon).build();
        ItemCouponsResult itemCoupons = OrderCouponResultFixture.anItemCoupons().itemCoupons(List.of(itemCouponResult)).build();

        CartCouponResult cartCouponResult = OrderCouponResultFixture.anCartCoupon().cartCoupon(cartCoupon).build();

        OrdererProfileResult ordererProfile = OrderUserResultFixture.anOrdererProfile().build();

        given(orderSheetRepository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderProductPort.getProducts(anyList())).willReturn(products);
        given(orderCouponPort.getItemCoupons(anyLong(), anyList())).willReturn(itemCoupons);
        given(orderCouponPort.getCartCoupon(anyLong(), anyLong())).willReturn(cartCouponResult);
        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfile);
        given(orderCommandService.saveOrder(any(CreateOrderContext.class))).willReturn(1L);
        //when
        OrderCreateResult result = orderFacade.createOrder(command);
        //then
        assertThat(result.orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다.")
    void createOrder_whenOrderSheetNotFound_thenThrownException() {
        //given
        CreateOrderCommand command = OrderCommandFixture.anCreateOrderCommand().build();
        given(orderSheetRepository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
    }

    @Test
    @DisplayName("주문서가 만료되었으면 예외가 발생한다.")
    void createOrder_whenOrderSheetExpired_thenThrownException() {
        //given
        CreateOrderCommand command = OrderCommandFixture.anCreateOrderCommand().build();

        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(20);
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given()
                .withShippingAddress(shippingAddress)
                .withExpiresAt(expiresAt)
                .build();

        given(orderSheetRepository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }
}
