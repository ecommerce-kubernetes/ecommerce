package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.external.OrderCouponGateway;
import com.example.order_service.order.application.external.OrderProductGateway;
import com.example.order_service.order.application.external.OrderUserGateway;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.service.order.dto.command.OrderCommand;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.repository.OrderSheetRepository;
import com.example.order_service.order.domain.vo.*;
import com.example.order_service.order.exception.OrderErrorCode;
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
import java.util.List;
import java.util.Optional;

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
    private OrderProductGateway orderProductGateway;
    @Mock
    private OrderCouponGateway orderCouponGateway;
    @Mock
    private OrderUserGateway orderUserGateway;
    @Mock
    private OrderValidator orderValidator;
    @Mock
    private PointUsagePolicy pointPolicy;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));


    @Nested
    @DisplayName("주문 생성")
    class InitialOrder{

        @Test
        @DisplayName("주문을 생성한다")
        void initialOrder(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(1L)
                    .build();
            OrderUserResult.UserPoint userPoint = Instancio.create(OrderUserResult.UserPoint.class);
            OrderProductResult.ProductList productResult = Instancio.create(OrderProductResult.ProductList.class);
            OrderCouponResult.Calculate couponResult = Instancio.create(OrderCouponResult.Calculate.class);
            OrderContext.CreateOrderContext orderContext = Instancio.create(OrderContext.CreateOrderContext.class);
            OrderResult.Create expectedResult = Instancio.create(OrderResult.Create.class);

            given(orderSheetRepository.findById(anyString())).willReturn(Optional.of(orderSheet));
            given(orderUserGateway.getUserPointsForOrder(anyLong(), any())).willReturn(userPoint);
            given(orderProductGateway.getProducts(anyList())).willReturn(productResult);
            given(orderCouponGateway.calculate(any())).willReturn(couponResult);
            given(orderMapper.toContext(any())).willReturn(orderContext);
            given(orderCommandService.saveOrder(any())).willReturn(expectedResult);
            //when
            OrderResult.Create result = orderFacade.initialOrder(command);
            //then
            assertThat(result).isEqualTo(expectedResult);
            then(orderProductGateway).should().getProducts(anyList());
            then(orderCouponGateway).should().calculate(any());
            then(orderValidator).should().validate(any(OrderSheet.class), any(OrderProductResult.ProductList.class),
                    any(OrderCouponResult.Calculate.class), any(OrderUserResult.UserPoint.class), any(PointUsagePolicy.class));
            then(orderUserGateway).should().getUserPointsForOrder(anyLong(), any());
            then(orderCommandService).should().saveOrder(any());
        }

        @Test
        @DisplayName("주문서를 찾을 수 없으면 예외가 발생한다")
        void initialOrder_not_found_sheet(){
            //given
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(999L)
                    .build();
            given(orderSheetRepository.findById(anyString())).willReturn(Optional.empty());
            //when
            //then
            assertThatThrownBy(() -> orderFacade.initialOrder(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_SHEET_NOT_FOUND);
        }

        @Test
        @DisplayName("주문자가 아닌 경우 예외가 발생한다")
        void initialOrder_no_permission(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(999L)
                    .build();
            given(orderSheetRepository.findById(anyString())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderFacade.initialOrder(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_ACCESS_DENIED);
        }

        @Test
        @DisplayName("주문서가 만료된 경우 예외가 발생한다")
        void initialOrder_sheet_expired(){
            //given
            OrderSheet orderSheet = createOrderSheet();
            ReflectionTestUtils.setField(orderSheet, "expiresAt", LocalDateTime.now(clock).minusMinutes(20));
            OrderCommand.Create command = OrderCommand.Create.builder()
                    .orderSheetId("sheetId")
                    .userId(1L)
                    .build();
            given(orderSheetRepository.findById(anyString())).willReturn(Optional.of(orderSheet));
            //when
            //then
            assertThatThrownBy(() -> orderFacade.initialOrder(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_EXPIRED);
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
        return OrderSheet.create("sheetId", orderer, shippingAddress, List.of(sheetItem), cartCoupon, LocalDateTime.now(clock), 30);
    }
}
