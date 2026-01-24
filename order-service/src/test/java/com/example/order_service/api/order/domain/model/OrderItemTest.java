package com.example.order_service.api.order.domain.model;

public class OrderItemTest {

//    @Test
//    @DisplayName("주문 상품 생성시 상품 옵션이 있는 경우 매핑되어 생성된다")
//    void createOrderItem(){
//        //given
//        OrderItemCreationContext.ItemOption itemOption = OrderItemCreationContext.ItemOption.builder()
//                .optionTypeName("사이즈")
//                .optionValueName("XL")
//                .build();
//
//        OrderItemCreationContext item = OrderItemCreationContext.builder()
//                .productId(1L)
//                .productVariantId(1L)
//                .sku("TS-NK-WH-XL")
//                .productName("상품1")
//                .thumbnailUrl("http://thumbnail.jpg")
//                .unitPrice(OrderItemCreationContext.UnitPrice.builder()
//                        .originalPrice(3000L)
//                        .discountRate(10)
//                        .discountAmount(300L)
//                        .discountedPrice(2700L)
//                        .build())
//                .quantity(3)
//                .lineTotal(8100L)
//                .itemOptions(List.of(itemOption))
//                .build();
//        //when
//        OrderItem orderItem = OrderItem.create(item);
//        //then
//        assertThat(orderItem)
//                .extracting("productId", "productVariantId", "sku", "productName", "originPrice", "discountRate",
//                        "discountAmount", "discountedPrice", "lineTotal", "quantity", "thumbnail")
//                        .contains(1L, 1L, "TS-NK-WH-XL", "상품1", 3000L, 10, 300L, 2700L, 8100L, 3, "http://thumbnail.jpg");
//        assertThat(orderItem.getItemOptions()).hasSize(1);
//        assertThat(orderItem.getItemOptions().get(0))
//                .extracting("optionTypeName", "optionValueName")
//                .containsExactly("사이즈", "XL");
//
//        assertThat(orderItem.getItemOptions().get(0).getOrderItem()).isEqualTo(orderItem);
//    }
//
//    @Test
//    @DisplayName("주문 상품 생성시 상품 옵션이 없는 경우 매핑하지 않은 객체를 생성한다")
//    void createOrderItem_withoutOptions(){
//        //given
//        OrderItemCreationContext item = OrderItemCreationContext.builder()
//                .productId(1L)
//                .productVariantId(1L)
//                .sku("TS-NK-WH-XL")
//                .productName("상품1")
//                .thumbnailUrl("http://thumbnail.jpg")
//                .unitPrice(OrderItemCreationContext.UnitPrice.builder()
//                        .originalPrice(3000L)
//                        .discountRate(10)
//                        .discountAmount(300L)
//                        .discountedPrice(2700L)
//                        .build())
//                .quantity(3)
//                .lineTotal(8100L)
//                .itemOptions(null)
//                .build();
//        //when
//        OrderItem orderItem = OrderItem.create(item);
//        //then
//        assertThat(orderItem)
//                .extracting("productId", "productVariantId", "sku", "productName", "originPrice", "discountRate",
//                        "discountAmount", "discountedPrice", "lineTotal", "quantity", "thumbnail")
//                .containsExactly(1L, 1L, "TS-NK-WH-XL", "상품1", 3000L, 10, 300L, 2700L, 8100L, 3, "http://thumbnail.jpg");
//        assertThat(orderItem.getItemOptions()).isEmpty();
//    }
}
