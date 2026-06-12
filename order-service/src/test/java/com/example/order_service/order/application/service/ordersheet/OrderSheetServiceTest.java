package com.example.order_service.order.application.service.ordersheet;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.policy.DefaultPointUsagePolicy;
import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
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
    private OrderSheetFactory factory = new OrderSheetFactory();
    @Spy
    private PointUsagePolicy pointUsagePolicy = new DefaultPointUsagePolicy(properties);

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("쿠폰을 적용한 경우 쿠폰 정보를 조회하고 주문서를 생성한다")
        void createOrderSheet_coupon_applied(){
            //given
            Long productVariantId = 1L;
            OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(1)
                    .build();
            OrderSheetCommand.ItemCoupon itemCouponCommand = OrderSheetCommand.ItemCoupon.builder()
                    .productVariantId(productVariantId)
                    .couponId(1L)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .cartCouponId(2L)
                    .itemCoupons(List.of(itemCouponCommand))
                    .build();
            OrderUserResult.Profile profile = Instancio.create(OrderUserResult.Profile.class);
            OrderProductResult.ProductList products = Instancio.of(OrderProductResult.ProductList.class)
                    .generate(field(OrderProductResult.ProductList::products), gen -> gen.collection().size(1))
                    .set(field(ProductSnapshot::getProductVariantId), productVariantId)
                    .create();
            OrderCouponResult.Calculate coupon = Instancio.of(OrderCouponResult.Calculate.class)
                    .generate(field(OrderCouponResult.Calculate::itemCoupons), gen -> gen.collection().size(1))
                    .set(field(OrderCouponResult.ItemCoupon::productVariantId), productVariantId)
                    .create();
            given(orderUserGateway.getUserProfile(any())).willReturn(profile);
            given(orderProductGateway.getProducts(anyList())).willReturn(products);
            given(orderCouponGateway.calculate(any())).willReturn(coupon);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderProductGateway).getProducts(anyList());
            verify(orderCouponGateway).calculate(any());
        }

        @Test
        @DisplayName("쿠폰을 적용하지 않은 경우 쿠폰 정보를 조회하지 않고 주문서를 생성한다")
        void createOrderSheet_coupon_not_applied() {
            //given
            Long productVariantId = 1L;
            OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(1)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .cartCouponId(null)
                    .itemCoupons(List.of())
                    .build();
            OrderUserResult.Profile profile = Instancio.create(OrderUserResult.Profile.class);
            OrderProductResult.ProductList products = Instancio.of(OrderProductResult.ProductList.class)
                    .generate(field(OrderProductResult.ProductList::products), gen -> gen.collection().size(1))
                    .set(field(ProductSnapshot::getProductVariantId), productVariantId)
                    .create();
            given(orderUserGateway.getUserProfile(any())).willReturn(profile);
            given(orderProductGateway.getProducts(anyList())).willReturn(products);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderProductGateway).getProducts(anyList());
            verify(orderCouponGateway, never()).calculate(any());
        }
    }

    @Nested
    @DisplayName("주문서 조회")
    class GetOrderSheet {

        @Test
        @DisplayName("주문서를 조회한다")
        void getOrderSheet(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            Money expectedAvailablePoints = orderSheet.calcAvailablePoints(point.ownedPoints(), pointUsagePolicy);
            given(repository.findById(anyString())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(anyLong())).willReturn(point);
            //when
            OrderSheetResult.Detail result = orderSheetService.getOrderSheet(orderSheet.getSheetId(), orderSheet.getOrderer().getUserId());
            //then
            assertThat(result.sheetId()).isEqualTo(orderSheet.getSheetId());
            assertThat(result.orderer().getUserId()).isEqualTo(orderSheet.getOrderer().getUserId());
            assertThat(result.point().availablePoints()).isEqualTo(expectedAvailablePoints);
        }

        @Test
        @DisplayName("주문서를 찾을 수 없는 경우 예외가 발생한다")
        void getOrderSheet_notFound(){
            //given
            String sheetId = "notFound";
            Long userId = 1L;
            given(repository.findById(any())).willReturn(Optional.empty());
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.getOrderSheet(sheetId, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문자가 아닌 경우 예외가 발생한다")
        void getOrderSheet_not_match_ordererId(){
            //given
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.getOrderSheet(orderSheet.getSheetId(), userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        @Test
        @DisplayName("주문서가 만료된 경우 예외가 발생한다")
        void getOrderSheet_expired(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now().minusMinutes(20));
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.getOrderSheet(orderSheet.getSheetId(), orderSheet.getOrderer().getUserId()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_EXPIRED);
        }
    }

    @Nested
    @DisplayName("배송 정보 수정")
    class UpdateShippingAddress {

        @Test
        @DisplayName("배송 정보를 수정한다")
        void updateShippingAddress(){
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            OrderSheet orderSheet = createOrderSheet();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPoints(any())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetService.updateShippingAddress(command);
            //then
            assertThat(result.sheetId()).isEqualTo(sheetId);
            assertThat(result.shippingAddress())
                    .extracting("receiverName", "receiverPhone", "zipCode", "address", "addressDetail")
                    .containsExactlyInAnyOrder(
                            command.receiverName(), command.receiverPhone(), command.zipCode(), command.address(), command.addressDetail()
                    );
        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void updateShippingAddress_notFound(){
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
            assertThatThrownBy(() -> orderSheetService.updateShippingAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updateShippingAddress_expired(){
            //given
            String sheetId = "sheetId";
            Long userId = 1L;
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now().minusMinutes(20));
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.updateShippingAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_EXPIRED);
        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updateShippingAddress_no_permission(){
            //given
            String sheetId = "sheetId";
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdateShippingAddress command = Instancio.of(OrderSheetCommand.UpdateShippingAddress.class)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::sheetId), sheetId)
                    .set(field(OrderSheetCommand.UpdateShippingAddress::userId), userId)
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.updateShippingAddress(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_ACCESS_DENIED);
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
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getSheetId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(usedPoints)
                    .build();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPointsForOrder(anyLong(), any())).willReturn(point);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Detail result = orderSheetService.updatePoints(command);
            //then
            verify(repository, times(1)).save(eq(orderSheet), any());
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(usedPoints);
        }

        @Test
        @DisplayName("사용 포인트가 주문에 적용할 수 있는 포인트를 초과하면 예외가 발생한다")
        void updatePoints_point_policy_violation(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getSheetId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(Money.wons(5000L))
                    .build();
            OrderUserResult.UserPoint point = Instancio.of(OrderUserResult.UserPoint.class)
                    .set(field(OrderUserResult.UserPoint::ownedPoints), Money.wons(10000L))
                    .create();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPointsForOrder(anyLong(), any())).willReturn(point);
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.updatePoints(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_POINT_POLICY_VIOLATION);

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
            assertThatThrownBy(() -> orderSheetService.updatePoints(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("주문자가 아니면 예외가 발생한다")
        void updatePoints_no_permission() {
            //given
            Long userId = 999L;
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getSheetId())
                    .userId(userId)
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.updatePoints(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        @Test
        @DisplayName("주문서가 만료되었으면 예외가 발생한다")
        void updatePoints_expired() {
            //given
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now().minusMinutes(20));
            OrderSheetCommand.UpdatePoints command = OrderSheetCommand.UpdatePoints.builder()
                    .sheetId(orderSheet.getSheetId())
                    .userId(orderSheet.getOrderer().getUserId())
                    .usedPoints(Money.wons(2000L))
                    .build();
            given(repository.findById(any())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderSheetService.updatePoints(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_EXPIRED);
        }
    }

    @Nested
    @DisplayName("상품 쿠폰 수정")
    class UpdateItemCoupon {

        @Test
        @DisplayName("상품 쿠폰을 해제한다")
        void updateItemCoupon_clear_coupon(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            String sheetItemId = orderSheet.getItems().get(0).getSheetItemId();
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(orderSheet.getSheetId(),
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
            OrderSheetResult.Detail result = orderSheetService.updateItemCoupon(command);
            //then
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(orderSheet.getTotalCouponDiscountAmount());
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(orderSheet.getUsedPoints());
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(orderSheet.getTotalPaymentAmount());
        }

        @Test
        @DisplayName("상품 쿠폰을 수정한다")
        void updateItemCoupon(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            String sheetItemId = orderSheet.getItems().get(0).getSheetItemId();
            Long targetProductVariantId = orderSheet.getItems().get(0).getProductVariantId();
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateItemCoupon command = OrderSheetCommand.UpdateItemCoupon.of(orderSheet.getSheetId(),
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
            OrderSheetResult.Detail result = orderSheetService.updateItemCoupon(command);
            //then
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(orderSheet.getTotalCouponDiscountAmount());
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(orderSheet.getUsedPoints());
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(orderSheet.getTotalPaymentAmount());
        }
    }

    @Nested
    @DisplayName("장바구니 쿠폰 변경")
    class UpdateCartCoupon {

        @Test
        @DisplayName("장바구니 쿠폰 해제")
        void updateCartCoupon_clear_coupon(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(orderSheet.getSheetId(),
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
            OrderSheetResult.Detail result = orderSheetService.updateCartCoupon(command);
            //then
            assertThat(result.cartCoupon().getCouponId()).isNull();
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(orderSheet.getTotalCouponDiscountAmount());
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(orderSheet.getUsedPoints());
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(orderSheet.getTotalPaymentAmount());
        }

        @Test
        @DisplayName("장바구니 쿠폰을 수정한다")
        void updateCartCoupon_point_not_used(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            Long newCouponId = 10L;
            OrderSheetCommand.UpdateCartCoupon command = OrderSheetCommand.UpdateCartCoupon.of(orderSheet.getSheetId(),
                    orderSheet.getOrderer().getUserId(), newCouponId);
            OrderCouponSnapshot cartCoupon = Instancio.of(OrderCouponSnapshot.class)
                    .set(field(OrderCouponSnapshot::getCouponId), newCouponId)
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
            OrderSheetResult.Detail result = orderSheetService.updateCartCoupon(command);
            //then
            assertThat(result.cartCoupon().getCouponId()).isEqualTo(newCouponId);
            assertThat(result.paymentSummary().totalCouponDiscount()).isEqualTo(orderSheet.getTotalCouponDiscountAmount());
            assertThat(result.paymentSummary().usedPoints()).isEqualTo(orderSheet.getUsedPoints());
            assertThat(result.paymentSummary().totalPaymentAmount()).isEqualTo(orderSheet.getTotalPaymentAmount());
        }
    }

    private OrderSheet createOrderSheet() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        ProductSnapshot product = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE", "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot price = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        OrderCouponSnapshot itemCoupon = OrderCouponSnapshot.of(1L, "하의 1000원 쿠폰", Money.wons(1000L));
        OrderCouponSnapshot cartCoupon = OrderCouponSnapshot.of(2L, "첫구매 1000원 할인 쿠폰", Money.wons(1000L));
        List<ProductOptionSnapshot> options = List.of(
                ProductOptionSnapshot.of("사이즈", "XL"),
                ProductOptionSnapshot.of("색상", "BLUE")
        );
        OrderSheetItem sheetItem = OrderSheetItem.create("sheetItemId", product, price, itemCoupon, 1, options);
        return OrderSheet.create("sheetId", orderer, shippingAddress, List.of(sheetItem), cartCoupon, LocalDateTime.now(), 30);
    }
}
