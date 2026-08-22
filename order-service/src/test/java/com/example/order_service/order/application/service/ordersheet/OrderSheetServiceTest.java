package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.application.port.*;
import com.example.order_service.order.application.port.dto.*;
import com.example.order_service.order.application.service.fixture.*;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.config.OrderSheetProperties;
import com.example.order_service.order.domain.ordersheet.*;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import com.example.order_service.order.domain.policy.*;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OrderSheetServiceTest {
    @InjectMocks
    private OrderSheetService orderSheetService;
    @Mock
    private OrderProductPort orderProductPort;
    @Mock
    private OrderCouponPort orderCouponPort;
    @Mock
    private OrderUserPort orderUserPort;
    @Mock
    private OrderCartPort orderCartPort;
    @Mock
    private OrderSheetRepository repository;
    @Spy
    private OrderSheetProperties properties = new OrderSheetProperties(30L, BigDecimal.valueOf(0.1));
    @Spy
    private PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(BigDecimal.valueOf(0.1));
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));
    @Spy
    private OrderValidator orderValidator;
    @Spy
    private IdGenerator idGenerator = new TsidGenerator();
    @Spy
    private OrderSheetContextFactory contextFactory;

    @Test
    @DisplayName("장바구니 주문서 생성")
    void createCartOrderSheet() {
        //given
        CreateCartOrderSheetCommand command = OrderSheetCommandFixture.anCreateCartCommand().build();

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = OrderUserResultFixture.anOrdererProfile().defaultShippingAddress(shippingAddress).build();

        OrderCartItemsResult cartResult = OrderCartResultFixture.anOrderCartItems().build();

        OrderProductsResult products = OrderProductResultFixture.anOrderProducts().build();

        LocalDateTime expectedExpiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());

        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfile);
        given(orderCartPort.getCartItems(anyLong(), anyList())).willReturn(cartResult);
        given(orderProductPort.getProducts(anyList())).willReturn(products);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetCreateResult result = orderSheetService.createCartOrderSheet(command);
        //then
        ArgumentCaptor<OrderSheet> orderSheetCaptor = ArgumentCaptor.forClass(OrderSheet.class);
        then(repository).should().save(orderSheetCaptor.capture(), any());

        OrderSheet capturedOrderSheet = orderSheetCaptor.getValue();

        assertThat(result.orderSheetId()).isNotNull();
        assertThat(result.expiresAt()).isEqualTo(expectedExpiresAt);
        assertThat(capturedOrderSheet.getShippingAddress()).isEqualTo(shippingAddress);
        assertThat(result.orderSheetId()).isNotNull();
    }

    @Test
    @DisplayName("장바구니 주문서 생성시, 주문자 정보의 대표 배송 정보가 없으면 배송정보가 없는 주문서가 생성된다.")
    void createCartOrderSheet_whenOrdererProfileWithoutDefaultShippingAddress_thenCreateOrderSheetWithoutShippingAddress() {
        //given
        CreateCartOrderSheetCommand command = OrderSheetCommandFixture.anCreateCartCommand().build();

        OrdererProfileResult ordererProfile = OrderUserResultFixture.anOrdererProfile().build();

        OrderCartItemsResult cartResult = OrderCartResultFixture.anOrderCartItems().build();

        OrderProductsResult products = OrderProductResultFixture.anOrderProducts().build();

        LocalDateTime expectedExpiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());

        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfile);
        given(orderCartPort.getCartItems(anyLong(), anyList())).willReturn(cartResult);
        given(orderProductPort.getProducts(anyList())).willReturn(products);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetCreateResult result = orderSheetService.createCartOrderSheet(command);
        //then
        ArgumentCaptor<OrderSheet> orderSheetCaptor = ArgumentCaptor.forClass(OrderSheet.class);
        then(repository).should().save(orderSheetCaptor.capture(), any());

        OrderSheet capturedOrderSheet = orderSheetCaptor.getValue();

        assertThat(result.orderSheetId()).isNotNull();
        assertThat(result.expiresAt()).isEqualTo(expectedExpiresAt);
        assertThat(capturedOrderSheet.getShippingAddress()).isNull();
    }

    @Test
    @DisplayName("바로 구매 주문서 생성")
    void createDirectOrderSheet() {
        //given
        CreateDirectOrderSheetCommand command = OrderSheetCommandFixture.anCreateDirectCommand().build();

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = OrderUserResultFixture.anOrdererProfile().defaultShippingAddress(shippingAddress).build();

        OrderProductsResult products = OrderProductResultFixture.anOrderProducts().build();

        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfile);
        given(orderProductPort.getProducts(anyList())).willReturn(products);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime expectedExpiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());

        //when
        OrderSheetCreateResult result = orderSheetService.createDirectOrderSheet(command);
        //then
        ArgumentCaptor<OrderSheet> orderSheetCaptor = ArgumentCaptor.forClass(OrderSheet.class);
        then(repository).should().save(orderSheetCaptor.capture(), any());

        OrderSheet capturedOrderSheet = orderSheetCaptor.getValue();

        assertThat(result.orderSheetId()).isNotNull();
        assertThat(result.expiresAt()).isEqualTo(expectedExpiresAt);
        assertThat(capturedOrderSheet.getShippingAddress()).isEqualTo(shippingAddress);
    }

    @Test
    @DisplayName("바로 구매 주문서 생성시, 주문자 정보의 대표 배송 정보가 없으면 배송정보가 없는 주문서가 생성된다.")
    void createDirectOrderSheet_whenOrdererProfileWithoutDefaultShippingAddress_thenCreateOrderSheetWithoutShippingAddress() {
        //given
        CreateDirectOrderSheetCommand command = OrderSheetCommandFixture.anCreateDirectCommand().build();

        OrdererProfileResult ordererProfile = OrderUserResultFixture.anOrdererProfile().build();

        OrderProductsResult products = OrderProductResultFixture.anOrderProducts().build();

        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfile);
        given(orderProductPort.getProducts(anyList())).willReturn(products);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime expectedExpiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());
        //when
        OrderSheetCreateResult result = orderSheetService.createDirectOrderSheet(command);
        //then
        ArgumentCaptor<OrderSheet> orderSheetCaptor = ArgumentCaptor.forClass(OrderSheet.class);
        then(repository).should().save(orderSheetCaptor.capture(), any());

        OrderSheet capturedOrderSheet = orderSheetCaptor.getValue();

        assertThat(result.orderSheetId()).isNotNull();
        assertThat(result.expiresAt()).isEqualTo(expectedExpiresAt);
        assertThat(capturedOrderSheet.getShippingAddress()).isNull();
    }

    @Test
    @DisplayName("주문서를 조회한다")
    void getOrderSheet() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        OrdererPointResult pointResult = OrderUserResultFixture.anOrdererPointResult().build();
        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderUserPort.getOrdererPoints(anyLong())).willReturn(pointResult);
        //when
        OrderSheetResult result = orderSheetService.getOrderSheet(orderSheet.getId(), orderSheet.getOrderer().getUserId());
        //then
        assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
        assertThat(result.orderer()).isEqualTo(orderSheet.getOrderer());
        assertThat(result.shippingAddress()).isEqualTo(orderSheet.getShippingAddress());

        assertThat(result.items())
                .extracting("price.lineTotal", "price.finalAmount")
                .containsExactly(
                        tuple(Money.wons(27000L), Money.wons(27000L))
                );

        assertThat(result.items().getFirst().coupon()).isNull();

        assertThat(result.cartCoupon()).isNull();

        assertThat(result.paymentSummary())
                .extracting("totalOriginalAmount", "totalItemDiscount", "totalItemCouponDiscount",
                        "cartCouponDiscount", "usedPoints", "totalPaymentAmount")
                .containsExactly(
                        Money.wons(30000L), Money.wons(3000L), Money.ZERO,
                        Money.ZERO, Money.ZERO, Money.wons(27000L)
                );

        assertThat(result.point())
                .extracting("availablePoints", "maxUsablePoints")
                .containsExactly(Money.wons(10000L), Money.wons(2700L));
    }

    @Test
    @DisplayName("주문서를 찾을 수 없는 경우 예외가 발생한다")
    void getOrderSheet_whenOrderSheetNotFound_thenThrownException() {
        //given
        Long orderSheetId = 999L;
        Long userId = 1L;
        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(orderSheetId, userId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
    }

    @Test
    @DisplayName("주문서가 만료된 경우 예외가 발생한다")
    void getOrderSheet_whenOrderSheetExpired_thenThrownException() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withExpiresAt(expiresAt).build();
        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(orderSheet.getId(), orderSheet.getOrderer().getUserId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }

    @Test
    @DisplayName("주문서 배송 정보를 변경한다")
    void updateShippingAddress() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();
        UpdateOrderSheetShippingAddressCommand command = OrderSheetCommandFixture.anUpdateShippingAddressCommand().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetUpdateResult result = orderSheetService.updateShippingAddress(command);
        //then
        assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
        assertThat(result.expiresAt()).isEqualTo(orderSheet.getExpiresAt());
        assertThat(orderSheet.getShippingAddress())
                .extracting("receiverName", "receiverPhone", "zipCode", "address", "addressDetail")
                .containsExactly(
                        command.receiverName(), command.receiverPhone(), command.zipCode(), command.address(), command.addressDetail()
                );
    }

    @Test
    @DisplayName("주문서 배송 정보를 변경할때 주문서를 찾을 수 없는 경우 예외가 발생한다.")
    void updateShippingAddress_whenOrderSheetNotFound_thenThrownException() {
        //given
        UpdateOrderSheetShippingAddressCommand command = OrderSheetCommandFixture.anUpdateShippingAddressCommand().build();
        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.updateShippingAddress(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
    }

    @Test
    @DisplayName("주문서 배송 정보를 변경할때 주문서가 만료된 경우 예외가 발생한다")
    void updateShippingAddress_whenOrderSheetExpired_thenThrownException() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withExpiresAt(expiresAt).build();
        UpdateOrderSheetShippingAddressCommand command = OrderSheetCommandFixture.anUpdateShippingAddressCommand().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.updateShippingAddress(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }

    @Test
    @DisplayName("상품 쿠폰을 적용한다.")
    void applyItemCoupons() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        OrderSheetItem orderSheetItem = orderSheet.getItems().stream().findFirst().orElseThrow();

        ApplyItemCouponsCommand.ItemCouponCommand itemCommand = OrderSheetCommandFixture.anItemCouponCommand().orderSheetItemId(orderSheetItem.getId()).build();
        ApplyItemCouponsCommand command = OrderSheetCommandFixture.anApplyItemCouponsCommand().itemCouponCommands(List.of(itemCommand)).build();

        ItemCouponsResult itemCouponsResult = OrderCouponResultFixture.anItemCoupons().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderCouponPort.getItemCoupons(anyLong(), anyList())).willReturn(itemCouponsResult);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetUpdateResult result = orderSheetService.applyItemCoupons(command);
        //then
        assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
        assertThat(result.expiresAt()).isEqualTo(orderSheet.getExpiresAt());

        assertThat(orderSheetItem)
                .extracting("itemCouponSnapshot.itemCouponId")
                .isEqualTo(1L);

        assertThat(orderSheetItem.calculateFinalAmount()).isEqualTo(Money.wons(26000L));

        assertThat(orderSheet.calculateTotalItemCouponDiscount()).isEqualTo(Money.wons(1000L));
        assertThat(orderSheet.calculateTotalPaymentAmount()).isEqualTo(Money.wons(26000L));

        assertThat(orderSheet.calculateMaxUsablePoints(pointUsagePolicy)).isEqualTo(Money.wons(2600L));
    }

    @Test
    @DisplayName("상품 쿠폰 적용 시, 주문서를 찾을 수 없으면 예외가 발생한다")
    void applyItemCoupons_whenOrderSheetNotFound_thenThrownException() {
        //given
        ApplyItemCouponsCommand command = OrderSheetCommandFixture.anApplyItemCouponsCommand().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyItemCoupons(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
    }

    @Test
    @DisplayName("상품 쿠폰 적용시 주문서가 만료된 경우 예외가 발생한다.")
    void applyItemCoupons_whenOrderSheetExpired_thenThrownException() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withExpiresAt(expiresAt).build();

        OrderSheetItem orderSheetItem = orderSheet.getItems().stream().findFirst().orElseThrow();

        ApplyItemCouponsCommand.ItemCouponCommand itemCommand = OrderSheetCommandFixture.anItemCouponCommand().orderSheetItemId(orderSheetItem.getId()).build();
        ApplyItemCouponsCommand command = OrderSheetCommandFixture.anApplyItemCouponsCommand().itemCouponCommands(List.of(itemCommand)).build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyItemCoupons(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }

    @Test
    @DisplayName("상품 쿠폰 적용시 주문 항목을 찾을 수 없는 경우 예외가 발생한다.")
    void applyItemCoupon_whenOrderSheetItemNotFound_thenThrownException() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        ApplyItemCouponsCommand.ItemCouponCommand itemCommand = OrderSheetCommandFixture.anItemCouponCommand().orderSheetItemId(999L).build();
        ApplyItemCouponsCommand command = OrderSheetCommandFixture.anApplyItemCouponsCommand().itemCouponCommands(List.of(itemCommand)).build();

        ItemCouponsResult itemCouponsResult = OrderCouponResultFixture.anItemCoupons().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderCouponPort.getItemCoupons(anyLong(), anyList())).willReturn(itemCouponsResult);
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyItemCoupons(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니 쿠폰을 적용한다")
    void applyCartCoupon() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        ApplyCartCouponCommand command = OrderSheetCommandFixture.anApplyCartCouponCommand().build();

        CartCouponResult cartCouponResult = OrderCouponResultFixture.anFixedCartCoupon().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderCouponPort.getCartCoupon(anyLong(), anyLong())).willReturn(cartCouponResult);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetUpdateResult result = orderSheetService.applyCartCoupon(command);
        //then
        assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
        assertThat(result.expiresAt()).isEqualTo(orderSheet.getExpiresAt());

        assertThat(orderSheet.getCartCoupon().getCartCouponId()).isEqualTo(1L);

        assertThat(orderSheet.calculateCartCouponDiscount()).isEqualTo(Money.wons(1000L));
        assertThat(orderSheet.calculateTotalPaymentAmount()).isEqualTo(Money.wons(26000L));

        assertThat(orderSheet.calculateMaxUsablePoints(pointUsagePolicy)).isEqualTo(Money.wons(2600L));
    }

    @Test
    @DisplayName("장바구니 쿠폰 쿠폰 적용 시, 주문서를 찾을 수 없으면 예외가 발생한다")
    void applyCartCoupon_whenOrderSheetNotFound_thenThrownException() {
        //given
        ApplyCartCouponCommand command = OrderSheetCommandFixture.anApplyCartCouponCommand().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyCartCoupon(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
    }

    @Test
    @DisplayName("장바구니 쿠폰 적용시 주문서가 만료된 경우 예외가 발생한다.")
    void applyCartCoupon_whenOrderSheetExpired_thenThrownException() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withExpiresAt(expiresAt).build();

        ApplyCartCouponCommand command = OrderSheetCommandFixture.anApplyCartCouponCommand().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyCartCoupon(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }

    @Test
    @DisplayName("포인트를 적용한다.")
    void applyPoints() {
        //given
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().build();

        ApplyPointCommand command = OrderSheetCommandFixture.anApplyPointCommand().build();

        OrdererProfileResult ordererProfileResult = OrderUserResultFixture.anOrdererProfile().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfileResult);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetUpdateResult result = orderSheetService.applyPoints(command);
        //then
        assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
        assertThat(result.expiresAt()).isEqualTo(orderSheet.getExpiresAt());

        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(1000L));
        assertThat(orderSheet.calculateTotalPaymentAmount()).isEqualTo(Money.wons(26000L));
    }

    @Test
    @DisplayName("포인트를 적용할 때, 주문서를 찾을 수 없으면 예외가 발생한다")
    void applyPoints_whenOrderSheetNotFound_thenThrownException() {
        //given
        ApplyPointCommand command = OrderSheetCommandFixture.anApplyPointCommand().build();
        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.empty());
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyPoints(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
    }

    @Test
    @DisplayName("포인트를 적용할 때, 주문서가 만료되었으면 예외가 발생한다")
    void applyPoints_whenOrderSheetExpired_thenThrownException() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given().withExpiresAt(expiresAt).build();

        ApplyPointCommand command = OrderSheetCommandFixture.anApplyPointCommand().build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyPoints(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }
}
