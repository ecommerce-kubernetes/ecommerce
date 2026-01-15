package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.api.option.domain.model.OptionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductOptionSpec extends BaseEntity {
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

    @Builder(access = AccessLevel.PRIVATE)
    private ProductOptionSpec(OptionType optionType, int priority){
        this.optionType = optionType;
        this.priority = priority;
    }

    public static ProductOptionSpec create(Product product, OptionType optionType, int priority) {
        ProductOptionSpec optionSpec = ProductOptionSpec.builder()
                .optionType(optionType)
                .priority(priority)
                .build();
        optionSpec.setProduct(product);
        return optionSpec;
    }

    protected void setProduct(Product product){
        this.product = product;
    }
}
