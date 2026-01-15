package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.api.option.domain.model.OptionValue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductVariantOption extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id")
    private OptionValue optionValue;

    public ProductVariantOption(OptionValue optionValue){
        this.optionValue = optionValue;
    }

    protected void setProductVariant(ProductVariant productVariant){
        this.productVariant = productVariant;
    }
}
