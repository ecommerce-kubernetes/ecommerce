package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.result.*;
import com.example.order_service.order.application.service.ordersheet.dto.command.CreateDirectOrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetCreateResult;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
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
    private OrderProductGateway orderProductGateway;
    @Mock
    private OrderCouponGateway orderCouponGateway;
    @Mock
    private OrderUserGateway orderUserGateway;
    @Mock
    private OrderSheetRepository repository;
    @Spy
    private OrderSheetProperties properties = new OrderSheetProperties(30L, BigDecimal.valueOf(0.1));
    @Spy
    private PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(properties);
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    @DisplayName("바로 구매 주문서 생성")
    void createDirectOrderSheet(){
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

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = createOrdererProfileResult(shippingAddress);

        OrderProductResult.OrderProductDetail product = createProductDetail(productVariantId, OrderProductStatus.ON_SALE, 100);
        OrderProductResult products = OrderProductResult.builder().products(List.of(product)).build();

        given(orderUserGateway.getOrdererProfile(userId)).willReturn(ordererProfile);
        given(orderProductGateway.getProducts(anyList())).willReturn(products);
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

        OrdererProfileResult ordererProfile = createOrdererProfileResult(null);

        OrderProductResult.OrderProductDetail product = createProductDetail(productVariantId, OrderProductStatus.ON_SALE, 100);
        OrderProductResult products = OrderProductResult.builder().products(List.of(product)).build();

        given(orderUserGateway.getOrdererProfile(userId)).willReturn(ordererProfile);
        given(orderProductGateway.getProducts(anyList())).willReturn(products);
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
    @DisplayName("바로 구매 주문서 생성시 주문 상품이 누락된 경우 예외가 발생한다.")
    void createDirectOrderSheet_missing_product() {
        //given
        Long userId = 1L;
        CreateDirectOrderSheetCommand.OrderVariant item1 = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CreateDirectOrderSheetCommand.OrderVariant item2 = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(2L)
                .quantity(3)
                .build();
        CreateDirectOrderSheetCommand command = CreateDirectOrderSheetCommand.builder()
                .userId(userId)
                .items(List.of(item1, item2))
                .build();

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = createOrdererProfileResult(shippingAddress);

        OrderProductResult.OrderProductDetail product1 = createProductDetail(1L, OrderProductStatus.ON_SALE, 100);
        OrderProductResult products = OrderProductResult.builder().products(List.of(product1)).build();
        given(orderUserGateway.getOrdererProfile(userId)).willReturn(ordererProfile);
        given(orderProductGateway.getProducts(anyList())).willReturn(products);
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.createDirectOrderSheet(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("바로 구매 주문서를 생성할때, 주문 상품이 주문 가능한 상태가 아니면 예외가 발생한다.")
    void createDirectOrderSheet_unOrderable_product() {
        //given
        Long userId = 1L;
        CreateDirectOrderSheetCommand.OrderVariant item = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CreateDirectOrderSheetCommand command = CreateDirectOrderSheetCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = createOrdererProfileResult(shippingAddress);

        OrderProductResult.OrderProductDetail product = createProductDetail(1L, OrderProductStatus.STOP_SALE, 100);
        OrderProductResult products = OrderProductResult.builder().products(List.of(product)).build();
        given(orderUserGateway.getOrdererProfile(userId)).willReturn(ordererProfile);
        given(orderProductGateway.getProducts(anyList())).willReturn(products);
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.createDirectOrderSheet(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
    }

    @Test
    @DisplayName("바로 구매 주문서를 생성할때, 주문 상품의 재고가 주문 수량보다 적으면 예외가 발생한다.")
    void createDirectOrderSheet_product_insufficient_stock() {
        //given
        Long userId = 1L;
        CreateDirectOrderSheetCommand.OrderVariant item = CreateDirectOrderSheetCommand.OrderVariant.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CreateDirectOrderSheetCommand command = CreateDirectOrderSheetCommand.builder()
                .userId(userId)
                .items(List.of(item))
                .build();

        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        OrdererProfileResult ordererProfile = createOrdererProfileResult(shippingAddress);

        OrderProductResult.OrderProductDetail product = createProductDetail(1L, OrderProductStatus.ON_SALE, 1);
        OrderProductResult products = OrderProductResult.builder().products(List.of(product)).build();
        given(orderUserGateway.getOrdererProfile(userId)).willReturn(ordererProfile);
        given(orderProductGateway.getProducts(anyList())).willReturn(products);
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.createDirectOrderSheet(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("주문서를 조회한다")
    void getOrderSheet() {
        //given
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(properties.ttlMinutes());
        OrderSheet orderSheet = createOrderSheet(expiresAt);

        OrdererPointResult pointResult = OrdererPointResult.builder().userId(1L).availablePoints(Money.wons(10000L)).build();
        given(repository.findByIdAndOrdererId(anyString(), anyLong())).willReturn(Optional.of(orderSheet));
        given(orderUserGateway.getOrdererPoints(anyLong())).willReturn(pointResult);
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
        String orderSheetId = "notFound";
        Long userId = 1L;
        given(repository.findByIdAndOrdererId(anyString(), anyLong())).willReturn(Optional.empty());
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
        given(repository.findByIdAndOrdererId(anyString(), anyLong())).willReturn(Optional.of(orderSheet));
        //when
        //then
        assertThatThrownBy(() -> orderSheetService.getOrderSheet(orderSheet.getId(), orderSheet.getOrderer().getUserId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_SHEET_EXPIRED);
    }

    @Test
    @DisplayName("주문서 배송 정보를 변경한다")
    void updateShippingAddress(){
        //given
        //when
        //then
    }

    private OrdererProfileResult createOrdererProfileResult(ShippingAddress shippingAddress) {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        return OrdererProfileResult.builder()
                .orderer(orderer)
                .defaultShippingAddress(shippingAddress)
                .build();
    }

    private OrderProductResult.OrderProductDetail createProductDetail(Long variantId, OrderProductStatus status, int stock) {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, variantId, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        ProductOptionSnapshot option1 = ProductOptionSnapshot.of("옵션1", "옵션 값");
        ProductOptionSnapshot option2 = ProductOptionSnapshot.of("옵션2", "옵션 값");
        return OrderProductResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .status(status)
                .stock(stock)
                .priceSnapshot(priceSnapshot)
                .options(List.of(option1, option2))
                .build();
    }

    @Nested
    @DisplayName("배송 정보 수정")
    class UpdateShippingAddress {

        @Test
        @DisplayName("배송 정보를 수정한다")
        void updateShippingAddress() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            String newPhone = "010-9876-5432";
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::receiverPhone), newPhone)
                    .create();
            OrderSheet orderSheet = createOrderSheetDeprecated();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(any())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void updateShippingAddress_notFound() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then

        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updateShippingAddress_expired() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheet orderSheet = createOrderSheetDeprecated();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now(clock).minusMinutes(20));
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then

        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updateShippingAddress_no_permission() {
            //given
            String sheetId = "sheetId";
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheetDeprecated();
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then

        }
    }

    @Nested
    @DisplayName("사용 포인트 수정")
    class UpdatePoints {

        @Test
        @DisplayName("사용 포인트를 수정한다")
        void updatePoints() {
            //given
            Money usedPoints = Money.wons(100L);
            OrderSheet orderSheet = createOrderSheetDeprecated();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(usedPoints)
                    .build();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(anyLong())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }

        @Test
        @DisplayName("사용 포인트가 주문에 적용할 수 있는 포인트를 초과하면 예외가 발생한다")
        void updatePoints_point_policy_violation() {
            //given
            OrderSheet orderSheet = createOrderSheetDeprecated();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(Money.wons(5000L))
                    .build();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(anyLong())).willReturn(point);
            //when
            //then

        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void updatePoints_notFound() {
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then
        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updatePoints_no_permission() {
            //given
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheetDeprecated();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then

        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updatePoints_expired() {
            //given
            OrderSheet orderSheet = createOrderSheetDeprecated();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now(clock).minusMinutes(20));
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
        }
    }

    @Nested
    @DisplayName("상품 쿠폰 수정")
    class UpdateItemCoupon {

        @Test
        @DisplayName("상품 쿠폰을 해제한다")
        void updateItemCoupon_clear_coupon() {
            //given
            OrderSheet orderSheet = createOrderSheetDeprecated();
            String sheetItemId = orderSheet.getItems().get(0).getId();
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(orderSheet.getId(),
                    sheetItemId, orderSheet.getOrderer().getUserId(), null);
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .set(field(OrderCouponResult.Calculate::itemCoupons), List.of())
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }

        @Test
        @DisplayName("상품 쿠폰을 수정한다")
        void updateItemCoupon() {
            //given
            OrderSheet orderSheet = createOrderSheetDeprecated();
            String sheetItemId = orderSheet.getItems().get(0).getId();
            Long targetProductVariantId = orderSheet.getItems().get(0).getProductVariantId();
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(orderSheet.getId(),
                    sheetItemId, orderSheet.getOrderer().getUserId(), newCouponId);
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .generate(field(OrderCouponResult.Calculate::itemCoupons), gen -> gen.collection().size(1))
                    .set(field(OrderCouponResult.ItemCoupon::productVariantId), targetProductVariantId)
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then

        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 변경")
    class UpdateCartCoupon {

        @Test
        @DisplayName("장바구니 쿠폰 해제")
        void updateCartCoupon_clear_coupon() {
            //given
            OrderSheet orderSheet = createOrderSheetDeprecated();
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(orderSheet.getId(),
                    orderSheet.getOrderer().getUserId(), null);
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .set(field(OrderCouponResult.Calculate::cartCoupon), null)
                    .set(field(OrderCouponResult.Calculate::itemCoupons), List.of())
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }

        @Test
        @DisplayName("장바구니 쿠폰을 수정한다")
        void updateCartCoupon_point_not_used() {
            //given
            OrderSheet orderSheet = createOrderSheetDeprecated();
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(orderSheet.getId(),
                    orderSheet.getOrderer().getUserId(), newCouponId);
            CartCouponSnapshot cartCoupon = Instancio.of(CartCouponSnapshot.class)
                    .set(field(CartCouponSnapshot::getCartCouponId), newCouponId)
                    .create();
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .set(field(OrderCouponResult.Calculate::cartCoupon), cartCoupon)
                    .set(field(OrderCouponResult.Calculate::itemCoupons), List.of())
                    .create();
            OrderUserResult.UserPoint userPoint = Instancio.of(OrderUserResult.UserPoint.class).create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            given(orderUserGateway.getUserPoints(any())).willReturn(userPoint);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            //then
        }
    }

    @Deprecated
    private OrderSheet createOrderSheetDeprecated() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
//        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
//        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        OrderSheetItem sheetItem = OrderSheetItem.create( product, price, 1, options);
        return OrderSheet.create(orderer, List.of(sheetItem),LocalDateTime.now(clock).plusMinutes(30));
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

        OrderSheetItem orderSheetItem = OrderSheetItem.create(product, price, 5, options);
        OrderSheet orderSheet = OrderSheet.create(orderer, List.of(orderSheetItem), expiresAt);

        orderSheet.changeShippingAddress(shippingAddress);
        orderSheet.applyItemCoupon(orderSheetItem.getId(), itemCoupon, pointUsagePolicy);
        orderSheet.applyCartCoupon(cartCoupon, pointUsagePolicy);

        orderSheet.applyPoints(Money.wons(1000L), pointUsagePolicy);
        return orderSheet;
    }
}
