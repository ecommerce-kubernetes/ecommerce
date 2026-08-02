package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.common.util.TsidGenerator;
import com.example.order_service.order.application.port.*;
import com.example.order_service.order.application.port.dto.*;
import com.example.order_service.order.application.service.validator.OrderValidator;
import com.example.order_service.order.application.service.ordersheet.dto.command.*;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetUpdateResult;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import com.example.order_service.order.domain.ordersheet.OrderSheetItem;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetContext;
import com.example.order_service.order.domain.ordersheet.context.CreateOrderSheetItemContext;
import com.example.order_service.order.domain.policy.*;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
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
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("장바구니 주문서 생성")
    void createCartOrderSheet() {
        //given
        CreateCartOrderSheetCommand command = CreateCartOrderSheetCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L))
                .build();

        Money availablePoints = Money.wons(10000L);
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = createOrdererProfileResult(shippingAddress, availablePoints);

        OrderCartItemsResult.Item cartItem = createCartItem(1L, 1L, 3);
        OrderCartItemsResult cartResult = OrderCartItemsResult.builder().items(List.of(cartItem)).build();

        OrderProductsResult.OrderProductDetail product = createProductDetail(1L, OrderProductStatus.ON_SALE, 100);
        OrderProductsResult products = OrderProductsResult.builder().products(List.of(product)).build();

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
    void createCartOrderSheet_without_default_shippingAddress() {
        //given
        CreateCartOrderSheetCommand command = CreateCartOrderSheetCommand.builder()
                .userId(1L)
                .cartItemIds(List.of(1L))
                .build();

        Money availablePoints = Money.wons(10000L);
        OrdererProfileResult ordererProfile = createOrdererProfileResult(null, availablePoints);

        OrderCartItemsResult.Item cartItem = createCartItem(1L, 1L, 3);
        OrderCartItemsResult cartResult = OrderCartItemsResult.builder().items(List.of(cartItem)).build();

        OrderProductsResult.OrderProductDetail product = createProductDetail(1L, OrderProductStatus.ON_SALE, 100);
        OrderProductsResult products = OrderProductsResult.builder().products(List.of(product)).build();

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
        Long userId = 1L;
        Long productVariantId = 1L;
        int quantity = 3;
        CreateDirectOrderSheetCommand.OrderVariant item = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
        CreateDirectOrderSheetCommand command = CreateDirectOrderSheetCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        Money availablePoints = Money.wons(10000L);
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = createOrdererProfileResult(shippingAddress, availablePoints);

        OrderProductsResult.OrderProductDetail product = createProductDetail(productVariantId, OrderProductStatus.ON_SALE, 100);
        OrderProductsResult products = OrderProductsResult.builder().products(List.of(product)).build();

        given(orderUserPort.getOrdererProfile(userId)).willReturn(ordererProfile);
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
    void createDirectOrderSheet_without_default_shippingAddress() {
        //given
        Long userId = 1L;
        Long productVariantId = 1L;
        int quantity = 3;
        CreateDirectOrderSheetCommand.OrderVariant item = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
        CreateDirectOrderSheetCommand command = CreateDirectOrderSheetCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        Money availablePoints = Money.wons(10000L);
        OrdererProfileResult ordererProfile = createOrdererProfileResult(null, availablePoints);

        OrderProductsResult.OrderProductDetail product = createProductDetail(productVariantId, OrderProductStatus.ON_SALE, 100);
        OrderProductsResult products = OrderProductsResult.builder().products(List.of(product)).build();

        given(orderUserPort.getOrdererProfile(userId)).willReturn(ordererProfile);
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
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());
        OrderSheet orderSheet = createOrderSheet(expiresAt);

        OrdererPointResult pointResult = OrdererPointResult.builder().userId(1L).availablePoints(Money.wons(10000L)).build();
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
                        tuple(Money.wons(45000L), Money.wons(44000L))
                );

        assertThat(result.items())
                .extracting("coupon.appliedDiscountAmount")
                .containsExactly(Money.wons(1000L));

        assertThat(result.cartCoupon().appliedDiscountAmount()).isEqualTo(Money.wons(1000L));

        assertThat(result.paymentSummary())
                .extracting("totalOriginalAmount", "totalItemDiscount", "totalItemCouponDiscount",
                        "cartCouponDiscount", "usedPoints", "totalPaymentAmount")
                .containsExactly(
                        Money.wons(50000L), Money.wons(5000L), Money.wons(1000L),
                        Money.wons(1000L), Money.wons(1000L), Money.wons(42000L)
                );

        assertThat(result.point())
                .extracting("availablePoints", "maxUsablePoints")
                .containsExactly(Money.wons(10000L), Money.wons(4300L));
    }

    @Test
    @DisplayName("주문서를 찾을 수 없는 경우 예외가 발생한다")
    void getOrderSheet_notFound() {
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
    void getOrderSheet_expired() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
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
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        UpdateOrderSheetShippingAddressCommand command = UpdateOrderSheetShippingAddressCommand.builder()
                .orderSheetId(orderSheet.getId())
                .userId(1L)
                .receiverName("수령자")
                .receiverPhone("010-9876-5432")
                .zipCode("12345")
                .address("서울시 테헤란로 321")
                .addressDetail("321동 1234호")
                .build();

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
    void updateShippingAddress_notFound_orderSheet() {
        //given
        UpdateOrderSheetShippingAddressCommand command = UpdateOrderSheetShippingAddressCommand.builder()
                .orderSheetId(999L)
                .userId(1L)
                .receiverName("수령자")
                .receiverPhone("010-9876-5432")
                .zipCode("12345")
                .address("서울시 테헤란로 321")
                .addressDetail("321동 1234호")
                .build();

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
    void updateShippingAddress_expired() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        UpdateOrderSheetShippingAddressCommand command = UpdateOrderSheetShippingAddressCommand.builder()
                .orderSheetId(orderSheet.getId())
                .userId(1L)
                .receiverName("수령자")
                .receiverPhone("010-9876-5432")
                .zipCode("12345")
                .address("서울시 테헤란로 321")
                .addressDetail("321동 1234호")
                .build();
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
        CouponDiscountPolicy couponPolicy = new RateCouponDiscountPolicy(50, Money.wons(100000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(10L, "바지 반값 할인 쿠폰", couponPolicy, 1);
        ItemCouponsResult.ItemCouponResult itemCouponResult = ItemCouponsResult.ItemCouponResult.builder()
                .status(OrderCouponStatus.AVAILABLE)
                .itemCoupon(itemCoupon)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();

        ItemCouponsResult itemCouponsResult = ItemCouponsResult.builder()
                .userId(1L)
                .itemCoupons(List.of(itemCouponResult))
                .build();

        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        OrderSheetItem orderSheetItem = orderSheet.getItems().stream().findFirst().orElseThrow();

        ApplyItemCouponsCommand.ItemCouponCommand itemCouponCommand = ApplyItemCouponsCommand.ItemCouponCommand.builder()
                .orderSheetItemId(orderSheetItem.getId())
                .itemCouponId(10L)
                .build();

        ApplyItemCouponsCommand command = ApplyItemCouponsCommand.builder()
                .userId(1L)
                .orderSheetId(orderSheet.getId())
                .itemCouponCommands(List.of(itemCouponCommand))
                .build();

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
                .isEqualTo(10L);

        assertThat(orderSheetItem.calculateFinalAmount()).isEqualTo(Money.wons(40500L));

        assertThat(orderSheet.calculateTotalItemCouponDiscount()).isEqualTo(Money.wons(4500L));
        assertThat(orderSheet.calculateTotalPaymentAmount()).isEqualTo(Money.wons(38500L));

        assertThat(orderSheet.calculateMaxUsablePoints(pointUsagePolicy)).isEqualTo(Money.wons(3950L));
    }

    @Test
    @DisplayName("상품 쿠폰 적용 시, 주문서를 찾을 수 없으면 예외가 발생한다")
    void applyItemCoupons_notFound_orderSheet() {
        //given
        ApplyItemCouponsCommand.ItemCouponCommand itemCouponCommand = ApplyItemCouponsCommand.ItemCouponCommand.builder()
                .orderSheetItemId(1L)
                .itemCouponId(10L)
                .build();

        ApplyItemCouponsCommand command = ApplyItemCouponsCommand.builder()
                .userId(1L)
                .orderSheetId(999L)
                .itemCouponCommands(List.of(itemCouponCommand))
                .build();

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
    void applyItemCoupons_expired_orderSheet() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        OrderSheetItem orderSheetItem = orderSheet.getItems().stream().findFirst().orElseThrow();

        ApplyItemCouponsCommand.ItemCouponCommand itemCouponCommand = ApplyItemCouponsCommand.ItemCouponCommand.builder()
                .orderSheetItemId(orderSheetItem.getId())
                .itemCouponId(10L)
                .build();

        ApplyItemCouponsCommand command = ApplyItemCouponsCommand.builder()
                .userId(1L)
                .orderSheetId(orderSheet.getId())
                .itemCouponCommands(List.of(itemCouponCommand))
                .build();

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
    void applyItemCoupon_notFound_orderSheetItem() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());
        OrderSheet orderSheet = createOrderSheet(expiresAt);

        ApplyItemCouponsCommand.ItemCouponCommand itemCouponCommand = ApplyItemCouponsCommand.ItemCouponCommand.builder()
                .orderSheetItemId(999L)
                .itemCouponId(10L)
                .build();

        ApplyItemCouponsCommand command = ApplyItemCouponsCommand.builder()
                .userId(1L)
                .orderSheetId(orderSheet.getId())
                .itemCouponCommands(List.of(itemCouponCommand))
                .build();

        CouponDiscountPolicy couponPolicy = new RateCouponDiscountPolicy(50, Money.wons(100000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(10L, "바지 반값 할인 쿠폰", couponPolicy, 1);
        ItemCouponsResult.ItemCouponResult itemCouponResult = ItemCouponsResult.ItemCouponResult.builder()
                .status(OrderCouponStatus.AVAILABLE)
                .itemCoupon(itemCoupon)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();
        ItemCouponsResult couponsResult = ItemCouponsResult.builder()
                .userId(1L)
                .itemCoupons(List.of(itemCouponResult))
                .build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderCouponPort.getItemCoupons(anyLong(), anyList())).willReturn(couponsResult);
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
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(10);
        OrderSheet orderSheet = createOrderSheet(expiresAt);

        ApplyCartCouponCommand command = ApplyCartCouponCommand.builder()
                .userId(1L)
                .cartCouponId(20L)
                .orderSheetId(orderSheet.getId())
                .build();

        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(20L, "장바구니 5% 할인 쿠폰", new RateCouponDiscountPolicy(5, Money.wons(50000L)), Money.wons(30000L));
        CartCouponResult cartCouponResult = CartCouponResult.builder().cartCoupon(cartCoupon).build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderCouponPort.getCartCoupon(anyLong(), anyLong())).willReturn(cartCouponResult);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetUpdateResult result = orderSheetService.applyCartCoupon(command);
        //then
        assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
        assertThat(result.expiresAt()).isEqualTo(orderSheet.getExpiresAt());

        assertThat(orderSheet.getCartCoupon().getCartCouponId()).isEqualTo(20L);

        assertThat(orderSheet.calculateCartCouponDiscount()).isEqualTo(Money.wons(2200L));
        assertThat(orderSheet.calculateTotalPaymentAmount()).isEqualTo(Money.wons(40800L));

        assertThat(orderSheet.calculateMaxUsablePoints(pointUsagePolicy)).isEqualTo(Money.wons(4180L));
    }

    @Test
    @DisplayName("장바구니 쿠폰 쿠폰 적용 시, 주문서를 찾을 수 없으면 예외가 발생한다")
    void applyCartCoupon_notFound_orderSheet() {
        //given
        ApplyCartCouponCommand command = ApplyCartCouponCommand.builder()
                .userId(1L)
                .cartCouponId(20L)
                .orderSheetId(999L)
                .build();

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
    void applyCartCoupon_expired_orderSheet() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = createOrderSheet(expiresAt);

        ApplyCartCouponCommand command = ApplyCartCouponCommand.builder()
                .userId(1L)
                .cartCouponId(20L)
                .orderSheetId(orderSheet.getId())
                .build();

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
        Long userId = 1L;
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(10);
        OrderSheet orderSheet = createOrderSheet(expiresAt);
        ApplyPointCommand command = ApplyPointCommand.builder()
                .orderSheetId(orderSheet.getId())
                .userId(userId)
                .usedPoints(2000L)
                .build();

        Money availablePoints = Money.wons(10000L);
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfileResult = createOrdererProfileResult(shippingAddress, availablePoints);
        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderUserPort.getOrdererProfile(anyLong())).willReturn(ordererProfileResult);
        given(repository.save(any(OrderSheet.class), any())).willAnswer(invocation -> invocation.getArgument(0));
        //when
        OrderSheetUpdateResult result = orderSheetService.applyPoints(command);
        //then
        assertThat(result.orderSheetId()).isEqualTo(orderSheet.getId());
        assertThat(result.expiresAt()).isEqualTo(orderSheet.getExpiresAt());

        assertThat(orderSheet.getUsedPoints()).isEqualTo(Money.wons(2000L));
        assertThat(orderSheet.calculateTotalPaymentAmount()).isEqualTo(Money.wons(41000L));
    }

    @Test
    @DisplayName("포인트를 적용할 때, 주문서를 찾을 수 없으면 예외가 발생한다")
    void applyPoints_orderSheet_notFound() {
        //given
        Long userId = 1L;
        ApplyPointCommand command = ApplyPointCommand.builder()
                .orderSheetId(999L)
                .userId(userId)
                .usedPoints(1000L)
                .build();
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
    void applyPoints_orderSheet_expired() {
        //given
        Long userId = 1L;
        LocalDateTime expiresAt = LocalDateTime.now(clock).minusMinutes(10);
        OrderSheet orderSheet = createOrderSheet(expiresAt);

        ApplyPointCommand command = ApplyPointCommand.builder()
                .userId(userId)
                .orderSheetId(orderSheet.getId())
                .usedPoints(2000L)
                .build();

        given(repository.findByIdAndOrdererId(anyLong(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.applyPoints(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }

    private OrdererProfileResult createOrdererProfileResult(ShippingAddress shippingAddress, Money availablePoints) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        return OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(availablePoints)
                .defaultShippingAddress(shippingAddress)
                .build();
    }

    private OrderProductsResult.OrderProductDetail createProductDetail(Long variantId, OrderProductStatus status, int stock) {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, variantId, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot option1 = ProductOptionSnapshot.of("옵션1", "옵션 값");
        ProductOptionSnapshot option2 = ProductOptionSnapshot.of("옵션2", "옵션 값");
        return OrderProductsResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .status(status)
                .stock(stock)
                .priceSnapshot(priceSnapshot)
                .options(List.of(option1, option2))
                .build();
    }

    private OrderCartItemsResult.Item createCartItem(Long cartItemId, Long variantId, int quantity) {
        return OrderCartItemsResult.Item.builder()
                .cartItemId(cartItemId)
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    private OrderSheet createOrderSheet(LocalDateTime expiresAt) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );

        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "청바지 1000원 할인", couponDiscountPolicy, 1);
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "장바구니 1000원 할인", couponDiscountPolicy, Money.wons(10000L));

        CreateOrderSheetItemContext itemCtx = CreateOrderSheetItemContext.builder()
                .productSnapshot(product)
                .priceSnapshot(price)
                .quantity(5)
                .optionSnapshots(options)
                .build();

        CreateOrderSheetContext context = CreateOrderSheetContext.builder()
                .orderer(orderer)
                .items(List.of(itemCtx))
                .expiresAt(expiresAt)
                .build();

        OrderSheet orderSheet = OrderSheet.create(context, idGenerator);

        OrderSheetItem item = orderSheet.getItems().getFirst();

        orderSheet.changeShippingAddress(shippingAddress);
        orderSheet.applyItemCoupon(item.getId(), itemCoupon, pointUsagePolicy);
        orderSheet.applyCartCoupon(cartCoupon, pointUsagePolicy);

        orderSheet.applyPoints(Money.wons(1000L), pointUsagePolicy);
        return orderSheet;
    }
}
