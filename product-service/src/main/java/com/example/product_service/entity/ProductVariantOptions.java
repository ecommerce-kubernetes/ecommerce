package com.example.product_service.entity;

import com.example.product_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductVariantOptions extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariants productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id")
    private OptionValues optionValue;

    public ProductVariantOptions(OptionValues optionValue){
        this.optionValue = optionValue;
    }

    protected void setProductVariant(ProductVariants productVariant){
        this.productVariant = productVariant;
    }
}
