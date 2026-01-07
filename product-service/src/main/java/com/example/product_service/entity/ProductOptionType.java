package com.example.product_service.entity;

import com.example.product_service.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductOptionType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_type_id")
    private OptionType optionType;

    private int priority;

    private boolean active;

    public ProductOptionType(OptionType optionType, int priority, boolean active){
        this.optionType = optionType;
        this.priority = priority;
        this.active = active;
    }

    protected void setProduct(Product product){
        this.product = product;
    }
}
