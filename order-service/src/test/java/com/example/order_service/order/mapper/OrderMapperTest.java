package com.example.order_service.order.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.mapper.OrderMapper;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderMapperTest {

    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    @Test
    @DisplayName("주문 생성 컨텍스트 매핑")
    void toContext() {
        //given
        OrderSheet orderSheet = createOrderSheet();
        //when
        OrderContext.CreateOrderContext context = orderMapper.toContext(orderSheet);
        //then
        assertThat(context.orderer()).usingRecursiveComparison().isEqualTo(orderSheet.getOrderer());
        assertThat(context.shippingAddress()).usingRecursiveComparison().isEqualTo(orderSheet.getShippingAddress());
        assertThat(context.cartCoupon()).usingRecursiveComparison().isEqualTo(orderSheet.getCartCoupon());

        OrderContext.ItemContext firstItemContext = context.orderItems().get(0);
        OrderSheetItem firstSheetItem = orderSheet.getItems().get(0);

        assertThat(firstItemContext.productSnapshot()).usingRecursiveComparison().isEqualTo(firstSheetItem.getProductSnapshot());
        assertThat(firstItemContext.itemPrice()).usingRecursiveComparison().isEqualTo(firstSheetItem.getPriceSnapshot());
        assertThat(firstItemContext.itemCouponSnapshot()).usingRecursiveComparison().isEqualTo(firstSheetItem.getItemCouponSnapshot());
        assertThat(firstItemContext.quantity()).isEqualTo(firstSheetItem.getQuantity());
    }

    private OrderSheet createOrderSheet() {
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
        return OrderSheet.create(orderer, List.of(sheetItem), LocalDateTime.now().plusMinutes(30));
    }
}
