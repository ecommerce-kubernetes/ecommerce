package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.category.domain.model.Category;
import com.example.product_service.api.common.entity.BaseEntity;
import com.example.product_service.api.common.exception.BusinessException;
import com.example.product_service.api.common.exception.ProductErrorCode;
import com.example.product_service.api.option.domain.model.OptionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

    public static final int MAX_OPTION_SPEC_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    @Enumerated(EnumType.STRING)
    private ProductStatus status;
    private String description;
    private LocalDateTime publishedAt;

    private String thumbnail;
    private Double rating = 0.0;
    private Long reviewCount = 0L;

    private Long displayPrice;
    private Long originalPrice;
    private Integer maxDiscountRate;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOptionSpec> optionSpecs = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Product(String name, Category category, ProductStatus status, String description, LocalDateTime publishedAt, String thumbnail, Double rating, Long reviewCount, Long displayPrice, Long originalPrice, Integer maxDiscountRate) {
        this.name = name;
        this.category = category;
        this.status = status;
        this.description = description;
        this.publishedAt = publishedAt;
        this.thumbnail = thumbnail;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.displayPrice = displayPrice;
        this.originalPrice = originalPrice;
        this.maxDiscountRate = maxDiscountRate;
    }

    public static Product create(String name, String description, Category category) {
        if (!category.isLeaf()) {
            throw new BusinessException(ProductErrorCode.CATEGORY_NOT_LEAF);
        }
        return Product.builder()
                .name(name)
                .category(category)
                .status(ProductStatus.PREPARING)
                .description(description)
                .rating(0.0)
                .reviewCount(0L)
                .build();
    }

    public void deleted() {
        this.status = ProductStatus.DELETED;
    }

    public void addVariant(ProductVariant productVariant) {
        this.variants.add(productVariant);
        productVariant.setProduct(this);
    }

    public void updateOptionSpecs(List<OptionType> newOptionTypes) {
        if (newOptionTypes.size() > MAX_OPTION_SPEC_COUNT) {
            throw new BusinessException(ProductErrorCode.EXCEED_OPTION_SPEC_COUNT);
        }

        Set<OptionType> uniqueTypes = new HashSet<>(newOptionTypes);
        if (uniqueTypes.size() != newOptionTypes.size()) {
            throw new BusinessException(ProductErrorCode.DUPLICATE_OPTION_TYPE);
        }
        optionSpecs.clear();
        for (int i = 0; i < newOptionTypes.size(); i++) {
            this.optionSpecs.add(
                    ProductOptionSpec.create(this, newOptionTypes.get(i), i+1)
            );
        }
    }
}
