package com.example.order_service.ordersheet.application;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.external.OrderSheetCouponGateway;
import com.example.order_service.ordersheet.application.external.OrderSheetProductGateway;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.vo.ProductStatus;
import com.example.order_service.ordersheet.domain.repository.OrderSheetRepository;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import com.example.order_service.ordersheet.infrastructure.config.OrderSheetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static com.example.order_service.support.TestFixtureUtil.sample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderSheetAppServiceTest {
    @InjectMocks
    private OrderSheetAppService orderSheetAppService;
    @Mock
    private OrderSheetProductGateway orderSheetProductGateway;
    @Mock
    private OrderSheetCouponGateway orderSheetCouponGateway;
    @Mock
    private OrderSheetRepository repository;
    @Spy
    private OrderSheetProperties properties = new OrderSheetProperties(30L);

    @Nested
    @DisplayName("주문서 저장")
    class Create {

        @Test
        @DisplayName("주문 상품 정보를 조회하여 주문서를 저장한다")
        void createOrderSheet() {
            //given
            Long targetVariantId = 1L;
            int quantity = 1;
            OrderSheetCommand.OrderItem orderItem = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(targetVariantId)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.ItemCoupon itemCoupon = OrderSheetCommand.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(2L)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(orderItem))
                    .cartCouponId(1L)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
            OrderSheetProductResult.Info productInfo = OrderSheetProductResult.Info.builder()
                    .productId(1L)
                    .productVariantId(targetVariantId)
                    .status(ProductStatus.ORDERABLE)
                    .sku("TEST-SKU")
                    .productName("테스트 상품")
                    .stock(100)
                    .originalPrice(Money.wons(9000L))
                    .discountRate(10)
                    .discountAmount(Money.ZERO)
                    .discountedPrice(Money.wons(9000L))
                    .build();
            OrderSheetCouponResult.CartCoupon cartCouponResult = OrderSheetCouponResult.CartCoupon.builder()
                    .couponId(1L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            OrderSheetCouponResult.ItemCoupon itemCouponResult = OrderSheetCouponResult.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(2L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            OrderSheetCouponResult.Calculate couponResult = OrderSheetCouponResult.Calculate.builder()
                    .cartCoupon(cartCouponResult)
                    .itemCoupons(List.of(itemCouponResult))
                    .build();

            given(orderSheetProductGateway.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            given(orderSheetCouponGateway.calculate(any())).willReturn(couponResult);
            given(repository.save(any(OrderSheet.class), any(Duration.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            //when
            OrderSheetResult.Default result = orderSheetAppService.createOrderSheet(command);
            //then
            assertThat(result.sheetId()).isNotNull();
            assertThat(result.expiresAt()).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.summary().totalBasePaymentAmount()).isEqualTo(Money.wons(7000L));
        }

        @Test
        @DisplayName("주문 상품이 주문 가능이 아니면 주문할 수 없다")
        void createOrderSheet_not_on_sale() {
            //given
            Long targetVariantId = 1L;
            int quantity = 10;
            //주문 command
            OrderSheetCommand.OrderItem orderItem = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(targetVariantId)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(orderItem))
                    .build();
            // 판매 중지된 상품
            OrderSheetProductResult.Info productInfo = sample(fixtureMonkey.giveMeBuilder(OrderSheetProductResult.Info.class)
                    .set("productVariantId", targetVariantId)
                    .set("status", ProductStatus.UNORDERABLE)
                    .set("stock", 100));
            given(orderSheetProductGateway.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.createOrderSheet(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_UNORDERABLE);
        }

        @Test
        @DisplayName("주문 상품 재고가 주문 수량보다 적으면 주문할 수 없다")
        void createOrderSheet_insufficient_stock() {
            //given
            Long targetVariantId = 1L;
            int quantity = 100;
            //주문 command
            OrderSheetCommand.OrderItem orderItem = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(targetVariantId)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(orderItem))
                    .build();

            //상품 조회 결과
            OrderSheetProductResult.Info productInfo = sample(fixtureMonkey.giveMeBuilder(OrderSheetProductResult.Info.class)
                    .set("productVariantId", targetVariantId)
                    .set("status", ProductStatus.ORDERABLE)
                    .set("stock", 10));

            given(orderSheetProductGateway.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.createOrderSheet(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_INSUFFICIENT_STOCK);
        }

        @Test
        @DisplayName("주문 상품이 누락된 경우 주문할 수 없다")
        void createOrderSheet_product_notFound(){
            //given
            Long targetVariantId = 1L;
            int quantity = 1;
            //주문 command
            OrderSheetCommand.OrderItem orderItem1 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(targetVariantId)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.OrderItem orderItem2 = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(2L)
                    .quantity(quantity)
                    .build();
            OrderSheetCommand.Create command = OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(orderItem1, orderItem2))
                    .build();

            OrderSheetProductResult.Info productInfo = sample(fixtureMonkey.giveMeBuilder(OrderSheetProductResult.Info.class)
                    .set("productVariantId", targetVariantId)
                    .set("status", ProductStatus.ORDERABLE)
                    .set("stock", 10));

            given(orderSheetProductGateway.getProducts(anyList()))
                    .willReturn(List.of(productInfo));
            //when
            //then
            assertThatThrownBy(() -> orderSheetAppService.createOrderSheet(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_PRODUCT_NOT_FOUND);
        }
    }
}
