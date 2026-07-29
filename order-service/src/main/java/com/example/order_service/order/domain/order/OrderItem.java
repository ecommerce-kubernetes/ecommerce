package com.example.order_service.order.domain.order;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.common.util.ProductOptionSnapshotConverter;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItem extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Embedded
    private ProductSnapshot product;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "discountAmount", column = @Column(name = "product_discount_amount"))
    })
    private ProductPriceSnapshot productPrice;

    @Embedded
    private AppliedItemCoupon appliedItemCoupon;

    private Integer quantity;

    @Convert(converter = ProductOptionSnapshotConverter.class)
    private List<ProductOptionSnapshot> options;

    @Embedded
    private OrderItemAmount orderItemAmount;

}

