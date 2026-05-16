package com.example.order_service.ordersheet.application;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetResult;
import com.example.order_service.ordersheet.application.external.OrderSheetCouponGateway;
import com.example.order_service.ordersheet.application.external.OrderSheetProductGateway;
import com.example.order_service.ordersheet.domain.repository.OrderSheetRepository;
import com.example.order_service.ordersheet.infrastructure.config.OrderSheetProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
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
        @DisplayName("쿠폰을 적용한 경우 쿠폰 정보를 조회하고 주문서를 생성한다")
        void createOrderSheet_coupon_applied(){
            //given
            OrderSheetCommand.Create command = createCouponAppliedCommand();
            List<OrderSheetProductResult.Info> products = createProducts();
            OrderSheetCouponResult.Calculate coupon = createCoupon();
            given(orderSheetProductGateway.getProducts(anyList())).willReturn(products);
            given(orderSheetCouponGateway.calculate(any())).willReturn(coupon);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetAppService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderSheetProductGateway).getProducts(anyList());
            verify(orderSheetCouponGateway).calculate(any());
        }

        @Test
        @DisplayName("쿠폰을 적용하지 않은 경우 쿠폰 정보를 조회하지 않고 주문서를 생성한다")
        void createOrderSheet_coupon_not_applied(){
            //given
            OrderSheetCommand.Create command = createNotCouponAppliedCommand();
            List<OrderSheetProductResult.Info> products = createProducts();
            given(orderSheetProductGateway.getProducts(anyList())).willReturn(products);
            when(repository.save(any(), any())).then(returnsFirstArg());
            //when
            OrderSheetResult.Create orderSheet = orderSheetAppService.createOrderSheet(command);
            //then
            assertThat(orderSheet.sheetId()).isNotNull();
            assertThat(orderSheet.expiresAt()).isNotNull();
            verify(orderSheetProductGateway).getProducts(anyList());
            verify(orderSheetCouponGateway, never()).calculate(any());
        }

        private OrderSheetCommand.Create createCouponAppliedCommand() {
            OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            OrderSheetCommand.ItemCoupon itemCoupon = OrderSheetCommand.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(1L)
                    .build();

            return OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .cartCouponId(2L)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
        }

        private OrderSheetCommand.Create createNotCouponAppliedCommand() {
            OrderSheetCommand.OrderItem item = OrderSheetCommand.OrderItem.builder()
                    .productVariantId(1L)
                    .quantity(1)
                    .build();
            return OrderSheetCommand.Create.builder()
                    .userId(1L)
                    .items(List.of(item))
                    .cartCouponId(null)
                    .itemCoupons(List.of())
                    .build();
        }

        private List<OrderSheetProductResult.Info> createProducts() {
            OrderSheetProductResult.Option size = OrderSheetProductResult.Option.builder()
                    .optionTypeName("사이즈")
                    .optionValueName("XL")
                    .build();
            OrderSheetProductResult.Option blue = OrderSheetProductResult.Option.builder()
                    .optionTypeName("색상")
                    .optionValueName("BLUE")
                    .build();
            OrderSheetProductResult.Info product = OrderSheetProductResult.Info.builder()
                    .productId(1L)
                    .productVariantId(1L)
                    .sku("PROD-XL-BLUE")
                    .productName("청바지")
                    .originalPrice(Money.wons(10000L))
                    .discountRate(10)
                    .discountAmount(Money.wons(1000L))
                    .discountedPrice(Money.wons(9000L))
                    .thumbnail("/product/product/jean_1.jpg")
                    .options(List.of(size, blue))
                    .build();
            return List.of(product);
        }

        private OrderSheetCouponResult.Calculate createCoupon() {
            OrderSheetCouponResult.CartCoupon cartCoupon = OrderSheetCouponResult.CartCoupon.builder()
                    .couponId(1L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            OrderSheetCouponResult.ItemCoupon itemCoupon = OrderSheetCouponResult.ItemCoupon.builder()
                    .productVariantId(1L)
                    .couponId(2L)
                    .couponName("1000원 할인 쿠폰")
                    .discountAmount(Money.wons(1000L))
                    .build();
            return OrderSheetCouponResult.Calculate
                    .builder()
                    .cartCoupon(cartCoupon)
                    .itemCoupons(List.of(itemCoupon))
                    .build();
        }
    }
}
